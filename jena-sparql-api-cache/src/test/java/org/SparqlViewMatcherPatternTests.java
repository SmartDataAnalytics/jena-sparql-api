package org;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;


@FixMethodOrder
public class SparqlViewMatcherPatternTests {
	protected static final SparqlQueryParser queryParser = SparqlQueryParserImpl.create(Syntax.syntaxSPARQL_10);
	protected static final SparqlElementParser elementParser = SparqlElementParserImpl.create(Syntax.syntaxSPARQL_10, null);

	public static QuadFilterPatternCanonical canonicalize(String elementStr) {
		Element element = elementParser.apply(elementStr);
		QuadFilterPatternCanonical result = canonicalize(element);
		return result;
	}


	public static QuadFilterPatternCanonical canonicalize(Element element) {
		ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(element);
        Generator<Var> generator = VarGeneratorImpl2.create();

        QuadFilterPatternCanonical result = SparqlCacheUtils.canonicalize2(pqfp.getQuadFilterPattern(), generator);

        return result;
	}


	public void tryMatch(String viewStr, String queryStr) {

		QuadFilterPatternCanonical viewQfpc = canonicalize(viewStr);
		QuadFilterPatternCanonical queryQpfc = canonicalize(queryStr);

        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQpfc);
        candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        System.out.println("Done.");
	}

	public boolean tryMatch(Element viewEl, Element queryEl) {
		QuadFilterPatternCanonical viewQfpc = canonicalize(viewEl);
		QuadFilterPatternCanonical queryQpfc = canonicalize(queryEl);

        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQpfc).peek(foo -> System.out.println(foo));
        boolean result = candidateSolutions.count() > 0;
        return result;


        //candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        //System.out.println("Done.");

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

		String str = r.getRequiredProperty(model.createProperty("http://ex.org/ontology/content"))
				.getObject().asLiteral().getString();
		Query result = queryParser.apply(str);
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
        for(Resource t : ts) {

        	String srcQueryId = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asLiteral().getString();
        	String tgtQueryId = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asLiteral().getString();
        	boolean expectedVerdict = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        	Query viewQuery = resolve(model, srcQueryId);
        	Query userQuery = resolve(model, tgtQueryId);

        	Element viewEl = viewQuery.getQueryPattern();
        	Element userEl = userQuery.getQueryPattern();

        	boolean actualVerdict = tryMatch(userEl, viewEl);
        	//System.out.println(srcQueryId + " - " + tgtQueryId + " - " + actualVerdict + " expected: "+ expectedVerdict);

        	Assert.assertEquals(expectedVerdict, actualVerdict);
        }
	}
}
