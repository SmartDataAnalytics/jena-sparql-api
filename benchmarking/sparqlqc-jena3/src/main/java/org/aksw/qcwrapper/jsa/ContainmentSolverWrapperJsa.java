package org.aksw.qcwrapper.jsa;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;

import com.google.common.base.Stopwatch;

import fr.inrialpes.tyrexmo.testqc.ContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.ContainmentTestException;

public class ContainmentSolverWrapperJsa
	implements ContainmentSolver
{
	protected BiFunction<QuadFilterPatternCanonical, QuadFilterPatternCanonical, Stream<Map<Var, Var>>> qfpcMatcher;

	public ContainmentSolverWrapperJsa(
			BiFunction<QuadFilterPatternCanonical, QuadFilterPatternCanonical, Stream<Map<Var, Var>>> qfpcMatcher) {
		super();
		this.qfpcMatcher = qfpcMatcher;
	}

	@Override
	public void warmup() throws ContainmentTestException {
		SparqlQueryContainmentUtils.tryMatch(
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
	}

	@Override
	public boolean entailed(Query q1, Query q2) throws ContainmentTestException {
		boolean result = SparqlQueryContainmentUtils.tryMatch(q2, q1, qfpcMatcher);
		return result;
	}

	@Override
	public void cleanup() throws ContainmentTestException {
		System.gc();
	}

	@Override
	public boolean entailedUnderSchema(String schema, Query q1, Query q2) throws ContainmentTestException {
		throw new ContainmentTestException("Cannot yet parse Jena Models");
	}

	@Override
	public boolean entailedUnderSchema(Model schema, Query q1, Query q2) throws ContainmentTestException {
		throw new ContainmentTestException("Cannot yet parse Jena Models");
	}

}
