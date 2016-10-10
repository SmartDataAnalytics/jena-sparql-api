package org.aksw.jena_sparql_api.cache.tests;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Stopwatch;


@FixMethodOrder
public class SparqlViewMatcherPatternTests {

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

	@Test
	public void test() throws IOException {
		/*
		tryMatch(
			String.join("\n",
				"?x <my://type> <my://Airport> .",
        		"?x <my://label> ?n ; ?h ?i . ",
        		"FILTER(langMatches(lang(?n), 'en')) .",
        		"FILTER(<mp://fn>(?x, ?n))"),

			String.join("\n",
	        		"?s <my://type> <my://Airport> .",
	        		"?s ?p ?l .",
	        		"FILTER(?p = <my://label> || ?p = <my://name>)")
		);
	*/
		Model tests = ModelFactory.createDefaultModel();
		RDFDataMgr.read(tests, new ClassPathResource("sparqlqc/1.4/benchmark/cqnoproj.rdf").getInputStream(), Lang.RDFXML);

        Model model = SparqlQcReader.readResources("sparqlqc/1.4/benchmark/noprojection/*");
        //model.write(System.out, "TURTLE");

        List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();

        Stopwatch sw = Stopwatch.createStarted();
        int j = 0;
        for(int i = 0; i < 1; ++i) {
	        for(Resource t : ts) {
	        	++j;

	        	String srcQueryId = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asLiteral().getString();
	        	String tgtQueryId = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asLiteral().getString();
	        	boolean expectedVerdict = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

	        	Query viewQuery = resolve(model, srcQueryId);
	        	Query userQuery = resolve(model, tgtQueryId);

	        	Element viewEl = viewQuery.getQueryPattern();
	        	Element userEl = userQuery.getQueryPattern();

	        	boolean actualVerdict = SparqlQueryContainmentUtils.tryMatch(userEl, viewEl);
	        	//System.out.println(srcQueryId + " - " + tgtQueryId + " - " + actualVerdict + " expected: "+ expectedVerdict);

	        	Assert.assertEquals(expectedVerdict, actualVerdict);
	        }
        }
        System.out.println("Avg: " + sw.elapsed(TimeUnit.MILLISECONDS) / (double)j + " - " + j);
	}
}
