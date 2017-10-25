package org.aksw.jena_sparql_api.cache.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.aksw.commons.collections.tagmap.TagMapSetTrie;
import org.aksw.commons.collections.tagmap.ValidationUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.SubgraphIsomorphismIndexJena;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage2;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsageAnalyzer2Visitor;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOp;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOpContainment;
import org.aksw.jena_sparql_api.query_containment.index.OpContext;
import org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex;
import org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.query_containment.index.ResidualMatching;
import org.aksw.jena_sparql_api.query_containment.index.TreeMapping;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.util.NodeUtils;
import org.jgrapht.DirectedGraph;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;


//@FixMethodOrder
//@Ignore
@RunWith(Parameterized.class)
public class SparqlViewMatcherPatternTests {

    private static final Logger logger = LoggerFactory.getLogger(SparqlViewMatcherPatternTests.class);


    @Parameters(name = "Query Containment {index}: {0}")
    public static Collection<Object[]> data()
            throws Exception
    {
        List<Object[]> params = new ArrayList<>();
        //params.addAll(createTestParams("sparqlqc/1.4/benchmark/cqnoproj.rdf"));
        params.addAll(createTestParams("sparqlqc/1.4/benchmark/ucqproj.rdf"));
        return params;
    }


    public static Collection<Object[]> createTestParams(String testCases) throws IOException {
        //Model tests = ModelFactory.createDefaultModel();
        //RDFDataMgr.read(tests, new ClassPathResource(testCases).getInputStream(), Lang.RDFXML);
        //Model model = SparqlQcReader.loadTasks(testCases, queries)readQueryFolder(queries);
        List<Resource> ts = SparqlQcReader.loadTasks(testCases);
        //List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();

        Object data[][] = new Object[ts.size()][3];
        for(int i = 0; i < ts.size(); ++i) {
            Resource t = ts.get(i);
            data[i][0] = t.getURI(); //testCase.getName();
            data[i][1] = t.getModel();
            data[i][2] = t;
        }

        Collection<Object[]> result = Arrays.asList(data);

        return result;
    }

    protected String name;
    protected Model model;
    protected Resource t;

    public SparqlViewMatcherPatternTests(String name, Model model, Resource resource) {
        this.name = name;
        this.model = model;
        this.t = resource;
    }


//    public Query resolve(Model model, String id) {
//    	Resource r = model.getResource(id);
//    	System.out.println("YAY: " + id);
//    	r.getModel().write(System.out, "TURTLE");
//
//		String str = r.getRequiredProperty(LSQ.text)
//				.getObject().asLiteral().getString();
//		Query result = SparqlQueryContainmentUtils.queryParser.apply(str);
//		return result;
//    }

//	public Query resolve(Model model, String id) {
//    	Matcher m = SparqlQcReader.queryNamePattern.matcher(id);
//    	m.find();
//    	String uri = "http://ex.org/query/" + m.group("id") + "-" + m.group("variant");
//    	Query result = extractQuery(model,  uri);
//    	return result;
//	}
//
//	public Query extractQuery(Model model, String uri) {
//		Resource r = model.getResource(uri);
//
//		String str = r.getRequiredProperty(LSQ.text)
//				.getObject().asLiteral().getString();
//		Query result = SparqlQueryContainmentUtils.queryParser.apply(str);
//		return result;
//	}

//
//	@Test
//	public void testProjection() throws IOException {
//		Model tests = ModelFactory.createDefaultModel();
//		RDFDataMgr.read(tests, new ClassPathResource("sparqlqc/1.4/benchmark/ucqproj.rdf").getInputStream(), Lang.RDFXML);
//
//        Model model = SparqlQcReader.readResources("sparqlqc/1.4/benchmark/projection/*");
//        List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();
//
//        for(Resource t : ts) {
//        	runTest(model, t);
//        }
//
//	}
//
//	@Test
//	public void test() throws IOException {
//		/*
//		tryMatch(
//			String.join("\n",
//				"?x <my://type> <my://Airport> .",
//        		"?x <my://label> ?n ; ?h ?i . ",
//        		"FILTER(langMatches(lang(?n), 'en')) .",
//        		"FILTER(<mp://fn>(?x, ?n))"),
//
//			String.join("\n",
//	        		"?s <my://type> <my://Airport> .",
//	        		"?s ?p ?l .",
//	        		"FILTER(?p = <my://label> || ?p = <my://name>)")
//		);
//	*/
//		Model tests = ModelFactory.createDefaultModel();
//		RDFDataMgr.read(tests, new ClassPathResource("sparqlqc/1.4/benchmark/cqnoproj.rdf").getInputStream(), Lang.RDFXML);
//
//        Model model = SparqlQcReader.readResources("sparqlqc/1.4/benchmark/noprojection/*");
//        //model.write(System.out, "TURTLE");
//
//        List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();
//
//        Stopwatch sw = Stopwatch.createStarted();
//        int j = 0;
//        for(int i = 0; i < 1; ++i) {
//	        for(Resource t : ts) {
//	        	++j;
//	        	runTest(model, t);
//	        }
//        }
//        System.out.println("Avg: " + sw.elapsed(TimeUnit.MILLISECONDS) / (double)j + " - " + j);
//	}

//	public void runTest(Model model, Resource t) {

    @Test
    public void runTest() {
        String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource().getRequiredProperty(SparqlQcVocab.sparqlQueryString).getObject().asLiteral().getString();
        String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource().getRequiredProperty(SparqlQcVocab.sparqlQueryString).getObject().asLiteral().getString();
        boolean expectedVerdict = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        Set<String> overrides = new HashSet<>(Arrays.asList(
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p24", // This is not the type of query we want to use for caching (the view is a union which partially matches into the user query)
            // TODO Fix the test case below:
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p26", // CARE! A view must not have more quad patterns than the query ; so the benchmark is correct - This consideration was WRONG: I think this is a bug in the benchmark; the expected result is wrong
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p27"  // Like p24; we require exact match of all of the views union members
        ));

        boolean overridden = overrides.contains(t.getURI());
        if(overridden) {
            expectedVerdict = !expectedVerdict;
        }


        Query viewQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);
        Query userQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);

        logger.debug("Test case: " + t);
        logger.debug("View Query: " + viewQuery);
        logger.debug("User Query: " + userQuery);

//		Element viewEl = viewQuery.getQueryPattern();
//		Element userEl = userQuery.getQueryPattern();


        //boolean actualVerdict = QueryToGraph.tryMatch(viewQuery, userQuery);

//		System.out.println("Hit a key to continue");
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}


        //VarMapper::createVarMapCandidates

//        boolean actualVerdict = SparqlQueryContainmentUtils.tryMatch(viewQuery, userQuery, QueryToGraph::match);
        
        
        boolean useOldCode = false;
        boolean actualVerdict;
        
        if(useOldCode) {
        	actualVerdict = SparqlQueryContainmentUtils.tryMatch(viewQuery, userQuery, VarMapper::createVarMapCandidates);
        } else {
        	actualVerdict = tryMatch(viewQuery, userQuery);	
        }
        logger.debug("Expected: " + expectedVerdict + " " + (overridden ? "(overridden)" : "") + " - Actual: " + actualVerdict + " Mismatch: " + (expectedVerdict != actualVerdict));

                //SparqlQueryContainmentUtils.tryMatch(userEl, viewEl);
        //System.out.println(srcQueryId + " - " + tgtQueryId + " - " + actualVerdict + " expected: "+ expectedVerdict);

        Assert.assertEquals(expectedVerdict, actualVerdict);
    }



    
    

    public static boolean tryMatch(Query view, Query user) {
    	
    	BiFunction<OpContext, OpContext, NodeMapperOp> nodeMapperFactory = (aContext, bContext) -> new NodeMapperOpContainment(aContext, bContext);
        
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexA = QueryContainmentIndexImpl.create(nodeMapper);
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexB = QueryContainmentIndexImpl.createFlat(nodeMapper);

        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiTreeTags = SubgraphIsomorphismIndexJena.create();
        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiFlat = SubgraphIsomorphismIndexJena.createFlat();
        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiTagBased = SubgraphIsomorphismIndexJena.createTagBased(new TagMapSetTrie<>(NodeUtils::compareRDFTerms));

        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiValidating = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiTreeTags, siiTagBased);
        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> sii = siiValidating;
        
        QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, ResidualMatching> index = QueryContainmentIndexImpl.create(sii, nodeMapperFactory);

 
        //view = QueryFactory.create("PREFIX ex: <http://ex.org/> SELECT * { ?s a ex:Person ; ex:name ?n . FILTER(contains(?n, 'fr')) }");
        //user = QueryFactory.create("PREFIX ex: <http://ex.org/> SELECT DISTINCT ?s { ?s a ex:Person ; ex:name ?n . FILTER(contains(?n, 'franz')) }");
        
        
        
        
        Node viewKey = NodeFactory.createURI("http://ex.org/view");
//        Op viewOp = Algebra.toQuadForm(Algebra.compile(view));
//        Op userOp = Algebra.toQuadForm(Algebra.compile(user));
        Op viewOp = Algebra.compile(view);
        Op userOp = Algebra.compile(user);

        

        {
        	Op op = QueryToGraph.normalizeOp(userOp, true);
        	//op = QueryToGraph.normalizeOp(op, true);
	        //VarUsageAnalyzer2Visitor varUsageAnalyzer = new VarUsageAnalyzer2Visitor();
	        Map<Op, VarUsage2> map = VarUsageAnalyzer2Visitor.analyze(op);
	        for(Entry<Op, VarUsage2> e : map.entrySet()) {
	        	System.out.println("VarUsage: " + e);
	        }
	        System.out.println("Normalized Op: " + op);
        }        
        
        

        
        index.put(viewKey, viewOp);


        List<Entry<Node, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching>>> matches = 
        		index.match(userOp).collect(Collectors.toList());

        System.out.println("Begin of matches:");
		for(Entry<Node, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching>> match : matches) {
        	System.out.println("  Match: " + match);
        }
        System.out.println("End of matches");
        
        boolean hasMatches = !matches.isEmpty();
        return hasMatches;
    }
}
