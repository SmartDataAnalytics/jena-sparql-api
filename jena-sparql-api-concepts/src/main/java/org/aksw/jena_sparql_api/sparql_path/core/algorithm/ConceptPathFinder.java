package org.aksw.jena_sparql_api.sparql_path.core.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.QueryGenerationUtils;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.sparql_path.core.PathConstraint;
import org.aksw.jena_sparql_api.sparql_path.core.VocabPath;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptPathFinder {

    private static final Logger logger = LoggerFactory.getLogger(ConceptPathFinder.class);


    public static ResultSet getPropertyAdjacency(QueryExecutionFactory qef) {
        //String queryStr = "Select Distinct ?x ?y { ?a ?x ?b . ?b ?y ?c }";
        // Exclude RDF memberships
        String queryStr = "Select Distinct ?x ?y { ?a ?x ?b . ?b ?y ?c . Filter(!regex(str(?x), '^http://www.w3.org/1999/02/22-rdf-syntax-ns#_') && !regex(str(?y), '^http://www.w3.org/1999/02/22-rdf-syntax-ns#_')) }";
        QueryExecution qe = qef.createQueryExecution(queryStr);
        ResultSet result = qe.execSelect();

        return result;
    }

    public static Model createJoinSummary(QueryExecutionFactory qef) {

        QueryExecution qe = qef.createQueryExecution("Select ?x ?y { ?x <" + VocabPath.joinsWith.getURI() + "> ?y }");

        ResultSet rs = qe.execSelect();

        Model result = processJoinSummaryQuery(rs);

        return result;
    }

    public static Model createDefaultJoinSummaryModel(QueryExecutionFactory qef) {

        ResultSet rs = getPropertyAdjacency(qef);

        Model result = processJoinSummaryQuery(rs);

        return result;
    }

    public static Model processJoinSummaryQuery(ResultSet rs) {
        Model joinSummaryModel = ModelFactory.createDefaultModel();

        while(rs.hasNext()) {
            QuerySolution qs = rs.next();

            Resource x = qs.getResource("x");
            Resource y = qs.getResource("y");

            joinSummaryModel.add(x, VocabPath.joinsWith, y);


//          String x = qs.get("x").asNode().getURI();
//          String y = qs.get("y").asNode().getURI();



            //System.out.println(x + "   " + y);
            //transitionGraph.addVertex(arg0);
        }
        logger.debug("Join summary model contains " + joinSummaryModel.size() + " triples");


        return joinSummaryModel;
    }

    public static List<SimplePath> findPaths(QueryExecutionFactory qef, UnaryRelation sourceConcept, UnaryRelation tmpTargetConcept, int nPaths, int maxHops) {
        Model joinSummaryModel = createDefaultJoinSummaryModel(qef);
        List<SimplePath> result = findPaths(qef, sourceConcept, tmpTargetConcept, nPaths, maxHops, joinSummaryModel);
        return result;
    }

    public static List<SimplePath> findPaths(QueryExecutionFactory qef, UnaryRelation sourceConcept, UnaryRelation tmpTargetConcept, int nPaths, int maxHops, Model joinSummaryModel) {

        /*
        if(joinSummaryModel == null) {
            joinSummaryModel = createDefaultJoinSummaryModel(qef);
        }*/


    	UnaryRelation targetConcept = ConceptUtils.makeDistinctFrom(tmpTargetConcept, sourceConcept);

        logger.debug("Distinguished target concept: " + targetConcept);



        //PathConstraint.getPathConstraintsSimple(targetConcept);

        //UndirectedGraph<String, EdgeTransition> transitionGraph = new SimpleGraph<String, EdgeTransition>(EdgeTransition.class);



        // Retrieve properties of the source concept
        // Example: If our source concept is ?s a Type", we do not know which properties the concept has

        Concept propertyConcept;
        if(sourceConcept.isSubjectConcept()) {
            List<Element> elements = sourceConcept.getElements();
            ElementTriplesBlock etb = (ElementTriplesBlock) elements.get(0);
            Triple triple = etb.getPattern().get(0);

            Var s = (Var)triple.getSubject();

            Var p = (Var)triple.getPredicate();

            ElementFilter pFilter = new ElementFilter(new E_Equals(new ExprVar(p), NodeValue.makeNode(RDF.type.asNode())));

            Var o = (Var)triple.getObject();
            ExprList oExprs = new ExprList();
            oExprs.add(NodeValue.makeNode(RDF.Property.asNode()));
            oExprs.add(NodeValue.makeNode(OWL.DatatypeProperty.asNode()));
            oExprs.add(NodeValue.makeNode(OWL.ObjectProperty.asNode()));

            ElementFilter oFilter = new ElementFilter(new E_OneOf(new ExprVar(o), oExprs));

            List<Element> newElements = new ArrayList<Element>();
            newElements.add(etb);
            newElements.add(pFilter);
            newElements.add(oFilter);

            propertyConcept = new Concept(newElements, s);

        } else {
            propertyConcept = QueryGenerationUtils.createPredicateQuery(sourceConcept);
        }

        Query propertyQuery = propertyConcept.asQuery();
        logger.debug("Property query: " + propertyQuery);


        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT * { ?s ?p ?o }").execSelect()));
        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("" + propertyQuery).execSelect()));
        List<Node> nodes = QueryExecutionUtils.executeList(qef, propertyQuery);
        logger.debug("Retrieved " + nodes.size() + " properties");// + nodes);


        // Add the start node to the transition model
        for(Node node : nodes) {

            // TODO Hack to see how this affects performance and quality
//		    if(node.getURI().startsWith("http://dbpedia.org/property/")) {
//		        continue;
//		    }

            Triple triple = new Triple(VocabPath.start.asNode(), VocabPath.joinsWith.asNode(), node);


//			System.out.println("JoinSummaryTriple: " + triple);
            Statement stmt = joinSummaryModel.asStatement(triple);
            joinSummaryModel.add(stmt);
        }

        QueryExecutionFactory qefMeta = new QueryExecutionFactoryModel(joinSummaryModel);

        // Now transform the target query so the find candidate nodes in the transition graph

        // Essentially:
        // ?moo prop1 ?foo . ?foo prop 2 ?bar .
        // becomes
        // Select ?s { ?s connectsTo ?prop1 . ?prop1 connectsTo ?foo }
        // In other words: we take the target concept, extract all quads

        //String test = "Prefix o:<http://foo.bar/> Prefix geo:<http://www.w3.org/2003/01/geo/wgs84_pos#> Select ?s { ?s o:connectsTo geo:long ; o:connectsTo geo:lat }";

        Concept targetCandidateConcept = PathConstraint.getPathConstraintsSimple(targetConcept);
        Query targetCandidateQuery = targetCandidateConcept.asQuery();

        //Query query = QueryFactory.create(test);
        logger.debug("TargetCandidateQuery: " + targetCandidateQuery);
        List<Node> candidates = QueryExecutionUtils.executeList(qefMeta, targetCandidateQuery);
        logger.debug("Got " + candidates.size() + " candidates: " + candidates);


        // Now that we know the candidates, we can start with out breath first search

        //DataSource ds = BreathFirstTask.createDb();


        // Convert the join summary to a jGraphT object
        Node startVertex = VocabPath.start.asNode();
        DefaultDirectedGraph<Node, DefaultEdge> graph = new DefaultDirectedGraph<Node, DefaultEdge>(DefaultEdge.class);


        graph.addVertex(startVertex);

        //graph.addVertex(startVertex);
        StmtIterator itStmt = joinSummaryModel.listStatements(null, VocabPath.joinsWith, (RDFNode)null);
        while(itStmt.hasNext()) {
            Statement stmt = itStmt.next();

            Node s = stmt.getSubject().asNode();
            Node o = stmt.getObject().asNode();

            //System.out.println(s + " --- " + s.equals(startVertex));

            graph.addVertex(s);
            graph.addVertex(o);
            graph.addEdge(s, o);
        }

        logger.debug("Graph Metrics: " + graph.vertexSet().size() + " vertices, " + graph.edgeSet().size() + " edges; based on (at least) " + joinSummaryModel.size() + " triples");

        //PathCallbackList callback = new PathCallbackList();
        //xx//KShortestPaths<Node, DefaultEdge> kShortestPaths = new KShortestPaths<Node, DefaultEdge>(graph, startVertex, nPaths, maxHops);

        List<GraphPath<Node, DefaultEdge>> candidateGraphPaths = new ArrayList<GraphPath<Node, DefaultEdge>>();
        int i = 0;
        for(Node candidate : candidates) {
            ++i;
            logger.debug("Processing candidate " + i + "/" + candidates.size() + ": " + candidate + " (nPaths = " + nPaths + ", maxHops = " + maxHops + ")");
            //Resource dest = joinSummaryModel.asRDFNode(candidate).asResource();

            if(startVertex.equals(candidate)) {
                GraphPath<Node, DefaultEdge> graphPath = new GraphWalk<Node, DefaultEdge>(graph, startVertex, candidate, new ArrayList<DefaultEdge>(), 0.0);
                candidateGraphPaths.add(graphPath);
            }
            else {
                DijkstraShortestPath<Node, DefaultEdge> dijkstraShortestPath = new DijkstraShortestPath<Node, DefaultEdge>(graph);
                GraphPath<Node, DefaultEdge> tmp = dijkstraShortestPath.getPath(startVertex, candidate);
                if(tmp != null) {
                    candidateGraphPaths.add(tmp);
                }


                // This code fires an exception if start equals target
                /*
                List<GraphPath<Node, DefaultEdge>> tmp = kShortestPaths.getPaths(candidate);
                if(tmp != null) {
                    candidateGraphPaths.addAll(tmp);
                }
                */
            }

            //NeighborProvider<Resource> np = new NeighborProviderModel(joinSummaryModel);


            //BreathFirstTask.run(np, VocabPath.start, dest, new ArrayList<Step>(), callback);
            //BreathFirstTask.runFoo(np, VocabPath.start, dest, new ArrayList<Step>(), new ArrayList<Step>(), callback);
        }

        Collections.sort(candidateGraphPaths, new GraphPathComparator<Node, DefaultEdge>());


        // Convert the graph paths to 'ConceptPaths'
        List<SimplePath> paths = new ArrayList<>();
        for(GraphPath<Node, DefaultEdge> graphPath : candidateGraphPaths) {

            Node current = graphPath.getStartVertex();

            List<P_Path0> steps = new ArrayList<>();

            for(DefaultEdge edge : graphPath.getEdgeList()) {
                Node source = graph.getEdgeSource(edge);
                Node target = graph.getEdgeTarget(edge);

                boolean isFwd;

                if(current.equals(source)) {
                    current = target;
                    isFwd = true;
                }
                else if(current.equals(target)) {
                    current = source;
                    isFwd = false;
                }
                else {
                    throw new RuntimeException("Should not happen");
                }

                P_Path0 step = PathUtils.createStep(current, isFwd);

                steps.add(step);
            }

            SimplePath path = new SimplePath(steps);
            paths.add(path);
        }


        //List<Path> paths = callback.getCandidates();

        // Cross check whether the path actually connects the source and target concepts
        Set<Var> varNames = new HashSet<>();
        varNames.addAll(sourceConcept.getVarsMentioned());
        varNames.addAll(targetConcept.getVarsMentioned());

        Generator<Var> generator = VarGeneratorBlacklist.create("v", varNames);

        List<SimplePath> result = new ArrayList<SimplePath>();

        for(SimplePath path : paths) {
            List<Element> pathElements = SimplePath.pathToElements(path, sourceConcept.getVar(), targetConcept.getVar(), generator);

            List<Element> tmp = new ArrayList<Element>();
            if(!sourceConcept.isSubjectConcept()) {
                tmp.addAll(sourceConcept.getElements());
            }

            // TODO Should we treat the case where the target concept is a subject concept in a special way?
            //if(!targetConcept.isSubjectConcept()) {
                tmp.addAll(targetConcept.getElements());
            //}

            tmp.addAll(pathElements);

            if(pathElements.isEmpty()) {
                if(!sourceConcept.getVar().equals(targetConcept.getVar()) && !sourceConcept.isSubjectConcept()) {
                    tmp.add(new ElementFilter(new E_Equals(new ExprVar(sourceConcept.getVar()), new ExprVar(targetConcept.getVar()))));
                }
            }

            ElementGroup group = new ElementGroup();
            for(Element t : tmp) {
                group.addElement(t);
            }

            Query query = new Query();
            query.setQueryAskType();
            query.setQueryPattern(group);

            logger.debug("Verifying candidate with query: " + query);

            QueryExecution xqe = qef.createQueryExecution(query);
            boolean isCandidate = xqe.execAsk();
            logger.debug("Verification result is [" + isCandidate + "] for " + query);

            if(isCandidate) {
                result.add(path);
            }
        }

        return result;
    }


    /**
     * Create a model with an RDF description of the found paths -
     * Used for SPARQL support
     *
     * @param paths
     * @return
     */
    public static Model createModel(List<SimplePath> paths) {
        Model result = ModelFactory.createDefaultModel();

        Resource Path = ResourceFactory.createResource("http://ns.aksw.org/jassa/ontology/Path");
        Property length = ResourceFactory.createProperty("http://ns.aksw.org/jassa/ontology/pathLength");

        int i = 0;
        for(SimplePath path : paths) {
            String pathStr = path.toPathString();

            Resource s = result.createResource("http://example.org/path/" + StringUtils.urlEncode(pathStr));
            Literal o = result.createLiteral(pathStr);
            Literal l = result.createTypedLiteral(path.getSteps().size());

            result.add(s, RDF.type, Path);
            result.add(s, RDFS.label, o);
            result.add(s, length, l);
        }

        return result;
    }
}
