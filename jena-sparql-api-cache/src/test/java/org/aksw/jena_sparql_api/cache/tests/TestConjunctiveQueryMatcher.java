package org.aksw.jena_sparql_api.cache.tests;

import java.util.Map;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concept_cache.dirty.ConjunctiveQueryMatcher;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConjunctiveQueryMatcherImpl;
import org.aksw.jena_sparql_api.concept_cache.dirty.QfpcMatch;
import org.aksw.jena_sparql_api.concept_cache.domain.ConjunctiveQuery;
import org.aksw.jena_sparql_api.concept_cache.op.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.junit.Assert;
import org.junit.Test;

public class TestConjunctiveQueryMatcher {

	protected ConjunctiveQuery cq = Stream.of("SELECT DISTINCT ?s { ?s a ?t }")
			.map(QueryFactory::create)
			.map(Algebra::compile)
			.map(Algebra::toQuadForm)
			.map(SparqlViewMatcherOpImpl::normalizeOp)
			.map(op -> (OpExtConjunctiveQuery)op)
			.map(OpExtConjunctiveQuery::getQfpc)
			//.map(op -> SparqlCacheUtils.tryExtractConjunctiveQuery(op, VarGeneratorImpl2.create()))
			.findFirst()
			.orElse(null);

	@Test
	public void testConjunctiveQueryExtraction() {
		//System.out.println(cq);
		Assert.assertNotNull(cq);
		// TODO Validate correctness thoroughly

		//System.out.println(cq);
	}

	@Test
	public void testConjunctiveQueryMatcher() {
		ConjunctiveQueryMatcher<String> matcher = new ConjunctiveQueryMatcherImpl<>();
		matcher.put("test", cq);

		Map<String, QfpcMatch> map = matcher.lookup(cq);
		System.out.println(map);
	}
}
