package org.aksw.jena_sparql_api.sparql_path.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.Step;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.QueryGenerationUtils;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.GraphPathComparator;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.GraphWalk;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConceptPathFinder {
	
	private static final Logger logger = LoggerFactory.getLogger(TestConceptPathFinder.class);

	
    /**
     * Takes a concept and adds
     * SELECT DISTINCT ?t { concept(s) . OPTIONAL { ?s a ?tmp } BIND(IF(BOUND(?tmp), ?tmp, eg:untyped) AS ?t) }
     *
     *
     *
     * @return
     */
    public static UnaryRelation createUnboundAwareTypeQuery(UnaryRelation concept) {
//        Set<Var> vars = concept.getVarsMentioned();
//        Var s = concept.getVar();

        Concept fragment = Concept.parse("?t | OPTIONAL { ?s a ?tmp } BIND(IF(BOUND(?tmp), ?tmp, eg:unbound) AS ?t)", PrefixMapping.Extended);
        UnaryRelation result = fragment
        		.injectOn(Vars.s)
        		.with(concept)
        		.project(fragment.getVars())
        		.toUnaryRelation();
        
        return result;
    }
	
    public static List<Path> findPaths(RDFConnection conn, UnaryRelation sourceConcept, UnaryRelation tmpTargetConcept, int nPaths, int maxHops, org.apache.jena.graph.Graph baseDataguide) {
    	UnaryRelation targetConcept = ConceptUtils.makeDistinctFrom(tmpTargetConcept, sourceConcept);

        logger.debug("Distinguished target concept: " + targetConcept);

        UnaryRelation typeConcept = createUnboundAwareTypeQuery(sourceConcept);
        
        Query propertyQuery = typeConcept.asQuery();
        logger.debug("Property query: " + propertyQuery);


        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT * { ?s ?p ?o }").execSelect()));
        //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("" + propertyQuery).execSelect()));
        List<Node> nodes = QueryExecutionUtils.executeList(new QueryExecutionFactorySparqlQueryConnection(conn), propertyQuery);
        logger.debug("Retrieved " + nodes.size() + " properties: " + nodes);

        org.apache.jena.graph.Graph ext = GraphFactory.createDefaultGraph();

        // Add the start node to the transition model
        for(Node node : nodes) {

            // TODO Hack to see how this affects performance and quality
//		    if(node.getURI().startsWith("http://dbpedia.org/property/")) {
//		        continue;
//		    }
        	
//        	Set<Node> types = baseDataguide.find(Node.ANY, VocabPath.hasOutgoingPredicate.asNode(), node)
//        		.mapWith(Triple::getSubject)
//        		.toSet();
//
//        	for(Node type : types) {
//                Triple triple = new Triple(VocabPath.start.asNode(), VocabPath.joinsWith.asNode(), type);
//                ext.add(triple);
//        	}

            Triple triple = new Triple(VocabPath.start.asNode(), VocabPath.joinsWith.asNode(), node);
            ext.add(triple);

//			System.out.println("JoinSummaryTriple: " + triple);
//            Statement stmt = joinSummaryGraph.asStatement(triple);
//            joinSummaryGraph.add(stmt);
        }

        
        org.apache.jena.graph.Graph union = new Union(baseDataguide, ext);
        Model joinSummaryGraph = ModelFactory.createModelForGraph(union);
        
		RDFDataMgr.write(System.out, joinSummaryGraph, RDFFormat.TURTLE_PRETTY);

        
        
        QueryExecutionFactory qefMeta = new QueryExecutionFactoryModel(joinSummaryGraph);

        
		Graph<Node, Triple> graph = new PseudoGraphJenaGraph(union);

        // Now transform the target query so the find candidate nodes in the transition graph

        // Essentially:
        // ?moo prop1 ?foo . ?foo prop 2 ?bar .
        // becomes
        // Select ?s { ?s connectsTo ?prop1 . ?prop1 connectsTo ?foo }
        // In other words: we take the target concept, extract all quads

        //String test = "Prefix o:<http://foo.bar/> Prefix geo:<http://www.w3.org/2003/01/geo/wgs84_pos#> Select ?s { ?s o:connectsTo geo:long ; o:connectsTo geo:lat }";

        Concept targetCandidateConcept = PathConstraint2.getPathConstraintsSimple(targetConcept);
        Query targetCandidateQuery = targetCandidateConcept.asQuery();

        //Query query = QueryFactory.create(test);
        logger.debug("TargetCandidateQuery: " + targetCandidateQuery);
        List<Node> candidates = QueryExecutionUtils.executeList(qefMeta, targetCandidateQuery);
        logger.debug("Got " + candidates.size() + " candidates: " + candidates);


        // Now that we know the candidates, we can start with out breath first search

        //DataSource ds = BreathFirstTask.createDb();


//        // Convert the join summary to a jGraphT object
        Node startVertex = VocabPath.start.asNode();
//        DefaultDirectedGraph<Node, DefaultEdge> graph = new DefaultDirectedGraph<Node, DefaultEdge>(DefaultEdge.class);
//
//
//        graph.addVertex(startVertex);
//
//        //graph.addVertex(startVertex);
//        StmtIterator itStmt = joinSummaryGraph.listStatements(null, VocabPath.joinsWith, (RDFNode)null);
//        while(itStmt.hasNext()) {
//            Statement stmt = itStmt.next();
//
//            Node s = stmt.getSubject().asNode();
//            Node o = stmt.getObject().asNode();
//
//            //System.out.println(s + " --- " + s.equals(startVertex));
//
//            graph.addVertex(s);
//            graph.addVertex(o);
//            graph.addEdge(s, o);
//        }
//
//        logger.debug("Graph Metrics: " + graph.vertexSet().size() + " vertices, " + graph.edgeSet().size() + " edges; based on (at least) " + joinSummaryGraph.size() + " triples");

        
        
        //PathCallbackList callback = new PathCallbackList();
        //xx//KShortestPaths<Node, DefaultEdge> kShortestPaths = new KShortestPaths<Node, DefaultEdge>(graph, startVertex, nPaths, maxHops);

        List<GraphPath<Node, Triple>> candidateGraphPaths = new ArrayList<GraphPath<Node, Triple>>();
        int i = 0;
        for(Node candidate : candidates) {
            ++i;
            logger.debug("Processing candidate " + i + "/" + candidates.size() + ": " + candidate + " (nPaths = " + nPaths + ", maxHops = " + maxHops + ")");
            //Resource dest = joinSummaryModel.asRDFNode(candidate).asResource();

            if(startVertex.equals(candidate)) {
                GraphPath<Node, Triple> graphPath = new GraphWalk<Node, Triple>(graph, startVertex, candidate, new ArrayList<Triple>(), 0.0);
                candidateGraphPaths.add(graphPath);
            }
            else {
            	AllDirectedPaths<Node, Triple> pathAlgo = new AllDirectedPaths<>(graph);
                //DijkstraShortestPath<Node, Triple> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
                //GraphPath<Node, Triple> tmp = dijkstraShortestPath.getPath(startVertex, candidate);
            	List<GraphPath<Node, Triple>> paths = pathAlgo.getAllPaths(startVertex, candidate, true, 10);
            	paths.forEach(p -> logger.debug("  " + p));
            	
            	candidateGraphPaths.addAll(paths);
//            	if(tmp != null) {
//                    candidateGraphPaths.add(tmp);
//                    System.out.println("  " + tmp);
//                }


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

        Collections.sort(candidateGraphPaths, new GraphPathComparator<>());


        // Convert the graph paths to 'ConceptPaths'
        List<Path> paths = new ArrayList<Path>();
        for(GraphPath<Node, Triple> graphPath : candidateGraphPaths) {

            Node current = graphPath.getStartVertex();

            List<Step> steps = new ArrayList<Step>();

            for(Triple edge : graphPath.getEdgeList()) {
                Node source = graph.getEdgeSource(edge);
                Node target = graph.getEdgeTarget(edge);

                boolean isInverse;

                if(current.equals(source)) {
                    current = target;
                    isInverse = false;
                }
                else if(current.equals(target)) {
                    current = source;
                    isInverse = true;
                }
                else {
                    throw new RuntimeException("Should not happen");
                }

                String propertyName = current.getURI();
                Step step = new Step(propertyName, isInverse);

                steps.add(step);
            }

            Path path = new Path(steps);
            paths.add(path);
        }


        //List<Path> paths = callback.getCandidates();

        // Cross check whether the path actually connects the source and target concepts
        Set<String> varNames = new HashSet<String>();
        varNames.addAll(VarUtils.getVarNames(PatternVars.vars(sourceConcept.getElement())));
        varNames.addAll(VarUtils.getVarNames(PatternVars.vars(targetConcept.getElement())));

        Generator generator = GeneratorBlacklist.create(Gensym.create("v"), varNames);

        List<Path> result = new ArrayList<Path>();

        for(Path path : paths) {
            List<Element> pathElements = Path.pathToElements(path, sourceConcept.getVar(), targetConcept.getVar(), generator);

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

            QueryExecution xqe = conn.query(query);
            boolean isCandidate = xqe.execAsk();
            logger.debug("Verification result is [" + isCandidate + "] for " + query);

            if(isCandidate) {
                result.add(path);
            }
        }

        return result;
    }

	
	
	@Test
	public void testConceptPathFinder() throws IOException, ParseException {
		InputStream in = TestConceptPathFinder.class.getClassLoader().getResourceAsStream("concept-path-finder.conf.sparql");
		Stream<SparqlStmt> stmts = SparqlStmtUtils.parse(in, SparqlStmtParserImpl.create(Syntax.syntaxARQ, true));

		Dataset ds = RDFDataMgr.loadDataset("concept-path-finder-test-data.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(ds);
		
		//stmts.map(stmt -> SparqlStmtUtils.process(conn, stmt));
		Model baseDataguide = ModelFactory.createDefaultModel();
		stmts
			.peek(System.out::println)
			.filter(SparqlStmt::isQuery)
			.map(SparqlStmt::getAsQueryStmt)
			.map(SparqlStmtQuery::getQuery)
			.filter(q -> q.isConstructType())
			.map(conn::queryConstruct)
			.forEach(baseDataguide::add);
		
		
		//Graph<RDFNode, Statement> g = new PseudoGraphJenaModel(model);
		
		
		List<Path> paths = findPaths(conn,
				//Concept.parse("?s | ?s ?p [ a eg:D ]", PrefixMapping.Extended),
				Concept.parse("?s | ?s eg:cd ?o", PrefixMapping.Extended),
				Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended),
				100, 100, baseDataguide.getGraph());
		
		System.out.println("Paths");
		paths.forEach(System.out::println);
//		KShortestPathAlgorithm<RDFNode, Statement> pathFinder = new KShortestSimplePaths<>(g);
//		pathFinder.getPaths(source, sink, k)
	}
}
