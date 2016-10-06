package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class SparqlQueryContainmentUtils {
	// TODO Move default parsers to some central place
	public static final SparqlQueryParser queryParser = SparqlQueryParserImpl.create(Syntax.syntaxSPARQL_10);
	public static final SparqlElementParser elementParser = SparqlElementParserImpl.create(Syntax.syntaxSPARQL_10, null);

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


	public static void tryMatch(String viewStr, String queryStr) {

		QuadFilterPatternCanonical viewQfpc = canonicalize(viewStr);
		QuadFilterPatternCanonical queryQpfc = canonicalize(queryStr);

        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQpfc);
        candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        System.out.println("Done.");
	}

	public static boolean tryMatch(Element viewEl, Element queryEl) {
		QuadFilterPatternCanonical viewQfpc = canonicalize(viewEl);
		QuadFilterPatternCanonical queryQpfc = canonicalize(queryEl);

        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQpfc).peek(foo -> System.out.println(foo));
        boolean result = candidateSolutions.count() > 0;
        return result;


        //candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        //System.out.println("Done.");

	}

	public static boolean tryMatch(Query viewQuery, Query userQuery) {
		Element viewEl = viewQuery.getQueryPattern();
		Element userEl = userQuery.getQueryPattern();

		boolean result = tryMatch(viewEl, userEl);
		return result;
	}
}