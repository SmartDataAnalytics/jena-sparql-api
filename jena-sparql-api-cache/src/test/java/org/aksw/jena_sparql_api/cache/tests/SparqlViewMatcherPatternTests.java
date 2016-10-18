package org.aksw.jena_sparql_api.cache.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;


//@FixMethodOrder
@RunWith(Parameterized.class)
public class SparqlViewMatcherPatternTests {

    @Parameters(name = "Query Containment {index}: {0}")
    public static Collection<Object[]> data()
            throws Exception
    {
    	List<Object[]> params = new ArrayList<>();
    	params.addAll(createTestParams("sparqlqc/1.4/benchmark/cqnoproj.rdf", "sparqlqc/1.4/benchmark/noprojection/*"));
    	params.addAll(createTestParams("sparqlqc/1.4/benchmark/ucqproj.rdf", "sparqlqc/1.4/benchmark/projection/*"));
    	return params;
    }


    public static Collection<Object[]> createTestParams(String testCases, String queries) throws IOException {
		Model tests = ModelFactory.createDefaultModel();
		RDFDataMgr.read(tests, new ClassPathResource(testCases).getInputStream(), Lang.RDFXML);
        Model model = SparqlQcReader.readResources(queries);
        List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();

        Object data[][] = new Object[ts.size()][3];
        for(int i = 0; i < ts.size(); ++i) {
        	Resource t = ts.get(i);
            data[i][0] = t.getURI(); //testCase.getName();
            data[i][1] = model;
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


	public Query resolve(Model model, String id) {
    	Matcher m = SparqlQcReader.queryNamePattern.matcher(id);
    	m.find();
    	String uri = "http://ex.org/query/" + m.group("id") + "-" + m.group("variant");
    	Query result = extractQuery(model,  uri);
    	return result;
	}

	public Query extractQuery(Model model, String uri) {
		Resource r = model.getResource(uri);

		String str = r.getRequiredProperty(LSQ.text)
				.getObject().asLiteral().getString();
		Query result = SparqlQueryContainmentUtils.queryParser.apply(str);
		return result;
	}

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
		String srcQueryId = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asLiteral().getString();
		String tgtQueryId = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asLiteral().getString();
		boolean expectedVerdict = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

		Query viewQuery = resolve(model, tgtQueryId);
		Query userQuery = resolve(model, srcQueryId);

		System.out.println("View Query: " + viewQuery);
		System.out.println("User Query: " + userQuery);

//		Element viewEl = viewQuery.getQueryPattern();
//		Element userEl = userQuery.getQueryPattern();

		boolean actualVerdict = SparqlQueryContainmentUtils.tryMatch(viewQuery, userQuery);
				//SparqlQueryContainmentUtils.tryMatch(userEl, viewEl);
		//System.out.println(srcQueryId + " - " + tgtQueryId + " - " + actualVerdict + " expected: "+ expectedVerdict);

		Assert.assertEquals(expectedVerdict, actualVerdict);
	}



}
