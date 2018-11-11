package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.Step;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.sparql_path.core.PathConstraint2;
import org.aksw.jena_sparql_api.sparql_path.core.VocabPath;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.GraphPathComparator;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class ConceptPathFinderBidirectionalUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ConceptPathFinderBidirectionalUtils.class);

	
	public static Single<Model> createDefaultDataSummary(SparqlQueryConnection dataConnection) {
		InputStream in = ConceptPathFinderBidirectionalUtils.class.getClassLoader().getResourceAsStream("concept-path-finder.conf.sparql");
		//Stream<SparqlStmt> stmts;
		Flowable<SparqlStmt> stmts;
		stmts = Flowable.fromIterable(() -> {
			try {
				return SparqlStmtUtils.parse(in, SparqlStmtParserImpl.create(Syntax.syntaxARQ, true)).iterator();
			} catch (IOException | ParseException e) {
				throw new RuntimeException(e);
			}
		});

		Single<Model> result = stmts
			//.peek(System.out::println)
			.filter(SparqlStmt::isQuery)
			.map(SparqlStmt::getAsQueryStmt)
			.map(SparqlStmtQuery::getQuery)
			.filter(q -> q.isConstructType())
			.map(dataConnection::queryConstruct)
			.toList()
			.map(list -> {
				Model r = ModelFactory.createDefaultModel();
				list.forEach(r::add);
				return r;
			});
		
		return result;
	}

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
        		.prependOn(Vars.s)
        		.with(concept)
        		//.project(fragment.getVars())
        		.toUnaryRelation();
        
        return result;
    }
	
    public static Flowable<Path> findPaths(SparqlQueryConnection conn, UnaryRelation sourceConcept, UnaryRelation tmpTargetConcept, Long nPaths, Long maxHops, org.apache.jena.graph.Graph baseDataguide) {
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

            Triple triple = new Triple(VocabPath.start.asNode(), VocabPath.connectsWith.asNode(), node);
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

        for(Node candidate : candidates) {
            Triple triple = new Triple(candidate, VocabPath.connectsWith.asNode(), VocabPath.end.asNode());
            ext.add(triple);
        }

        // Now that we know the candidates, we can start with out breath first search

        //DataSource ds = BreathFirstTask.createDb();


//        // Convert the join summary to a jGraphT object
        Node startVertex = VocabPath.start.asNode();
        Node endVertex = VocabPath.end.asNode();
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

//        List<GraphPath<Node, Triple>> candidateGraphPaths = new ArrayList<GraphPath<Node, Triple>>();
//        int i = 0;
//        for(Node candidate : candidates) {
//            ++i;
//            logger.debug("Processing candidate " + i + "/" + candidates.size() + ": " + candidate + " (nPaths = " + nPaths + ", maxHops = " + maxHops + ")");
            //Resource dest = joinSummaryModel.asRDFNode(candidate).asResource();

//            if(startVertex.equals(candidate)) {
//                GraphPath<Node, Triple> graphPath = new GraphWalk<Node, Triple>(graph, startVertex, candidate, new ArrayList<Triple>(), 0.0);
//                candidateGraphPaths.add(graphPath);
//            }
//            else {
//            	AllDirectedPaths<Node, Triple> pathAlgo = new AllDirectedPaths<>(graph);
//            	List<GraphPath<Node, Triple>> candidateGraphPaths = pathAlgo.getAllPaths(startVertex, VocabPath.end.asNode(), true, 10);

            	
            	//DijkstraShortestPath<Node, Triple> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
                //GraphPath<Node, Triple> tmp = dijkstraShortestPath.getPath(startVertex, candidate);
        
        List<GraphPath<Node, Triple>> candidateGraphPaths;
    	Integer _maxPathLength = maxHops == null ? null : maxHops.intValue();

    	int n = nPaths == null ? - 1 : Ints.checkedCast(nPaths);

    	if(nPaths == null) {
        	AllDirectedPaths<Node, Triple> pathAlgo = new AllDirectedPaths<>(graph);
        	candidateGraphPaths = pathAlgo.getAllPaths(startVertex, VocabPath.end.asNode(), true, _maxPathLength);
        } else {
        	
        	if(n <= 0) {
            	candidateGraphPaths = Collections.emptyList();         		
        	} else {
                KShortestPathAlgorithm<Node, Triple> kShortestPaths = new KShortestSimplePaths<>(graph, n);
                candidateGraphPaths = kShortestPaths.getPaths(startVertex, endVertex, n);
        	}                	
        }
        
        if(n >= 0) {
        	candidateGraphPaths = candidateGraphPaths.subList(0, Math.min(n, candidateGraphPaths.size()));
        }
        
        		candidateGraphPaths.forEach(p -> logger.debug("  " + p));
            	
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
//            }

            //NeighborProvider<Resource> np = new NeighborProviderModel(joinSummaryModel);


            //BreathFirstTask.run(np, VocabPath.start, dest, new ArrayList<Step>(), callback);
            //BreathFirstTask.runFoo(np, VocabPath.start, dest, new ArrayList<Step>(), new ArrayList<Step>(), callback);
        //}

        Collections.sort(candidateGraphPaths, new GraphPathComparator<>());


        // Convert the graph paths to 'ConceptPaths'        
        List<Path> paths = candidateGraphPaths.stream()
        	.map(ConceptPathFinderBidirectionalUtils::convertGraphPathToSparqlPath)
        	.filter(x -> x != null)
        	.collect(Collectors.toList());

        //List<Path> paths = callback.getCandidates();

        // Cross check whether the path actually connects the source and target concepts
        Set<Var> vars = new HashSet<>();
        vars.addAll(sourceConcept.getVarsMentioned());
        vars.addAll(targetConcept.getVarsMentioned());

        Generator<Var> generator = VarGeneratorBlacklist.create("v", vars);

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

        return Flowable.fromIterable(result);
    }

    
    /**
     * 
     * @param paths
     * @return (LinkedHash)Set of paths that validated
     */
    public static Set<Path> validatePaths(
    		Generator<Var> generator,
    		UnaryRelation sourceConcept,
    		UnaryRelation targetConcept,
    		RDFConnection conn,
    		Collection<Path> paths) {
    	Set<Path> result = paths.stream()
    		.filter(path -> ConceptPathFinderBidirectionalUtils.validatePath(generator, sourceConcept, targetConcept, conn, path))
    		.collect(Collectors.toCollection(LinkedHashSet::new));
    	return result;
    }
    
    

    public static boolean validatePath(
    		Generator<Var> generator,
    		UnaryRelation sourceConcept,
    		UnaryRelation targetConcept,
    		RDFConnection conn,
    		Path path) {
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

        
        return isCandidate;
//        if(isCandidate) {
//            result.add(path);
//        }
    }
	
    public static Boolean isFwd(Node p) {
        Boolean result =
        		VocabPath.hasOutgoingPredicate.asNode().equals(p) ? (Boolean)true :
        		VocabPath.hasIngoingPredicate.asNode().equals(p) ? (Boolean)false :
        		null;

        return result;
    }

    
    public static Path convertGraphPathToSparqlPath(GraphPath<Node, Triple> graphPath) {
    	
        List<Triple> el = graphPath.getEdgeList();
        List<Triple> effectiveEdgeList = el.subList(1, el.size() - 1);
        
        Path result = null;
        try {
	        List<Step> steps = Streams.mapWithIndex(effectiveEdgeList.stream(), Maps::immutableEntry)
	        	.filter(e -> e.getValue() % 2 == 0)
	        	.map(e -> e.getKey())
	        	// We may get a NPE here if t.getPredicate() could not be classified
	        	.map(t -> new Step(t.getObject().getURI(), !isFwd(t.getPredicate())))
	        	.collect(Collectors.toList())
	        	;

	        result = new Path(steps);
        } catch(Exception e) {
        	logger.debug("Harmless exception - but may indicate a bug in the algo or issue with input data: ", e);
        }
        
//        effectiveEdgeList
//        	.stream().fi
//        for(int i = 0; i < effectiveEdgeList.size(); i += 2) {
//        	Triple t = effectiveEdgeList.get(i);
//        	
//        	Node p = t.getPredicate();
//            String propertyName = t.getObject().getURI();
//
//            Boolean isFwd =
//            		VocabPath.hasOutgoingPredicate.asNode().equals(p) ? (Boolean)true :
//            		VocabPath.hasIngoingPredicate.asNode().equals(p) ? (Boolean)false :
//            		null;
//
//            if(isFwd == null) {
//            	continue;
//            }
//
//            Step step = new Step(propertyName, !isFwd);
//            steps.add(step);
//        }
//        
//        Path result = new Path(steps);
        return result;

    }
//        for(Triple edge : graphPath.getEdgeList()) {
//            Node source = graph.getEdgeSource(edge);
//            Node target = graph.getEdgeTarget(edge);
//
//            boolean isInverse;
//
//            if(current.equals(source)) {
//                current = target;
//                isInverse = false;
//            }
//            else if(current.equals(target)) {
//                current = source;
//                isInverse = true;
//            }
//            else {
//                throw new RuntimeException("Should not happen");
//            }
//
//            String propertyName = current.getURI();
//            Step step = new Step(propertyName, isInverse);
//
//            steps.add(step);
//        }

	
}
