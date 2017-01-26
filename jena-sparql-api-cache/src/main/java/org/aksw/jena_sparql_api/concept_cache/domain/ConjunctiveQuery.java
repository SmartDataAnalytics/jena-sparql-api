package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.ArrayList;

import org.aksw.jena_sparql_api.concept_cache.core.VarInfo;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpProject;

/**
 * Note: The reason why SparqlConjunctiveQuery and
 * QuadFilterPatternCanonical are not the same object (i.e. we could
 * make the projection part of the QFPC), is, that
 * one QFPC may be indexed with different projections.
 *
 * Via OpConjunctiveQuery, conjunctive queries can be placed
 * into SPARQL algebra expressions.
 *
 * @author raven
 *
 */
public class ConjunctiveQuery {
	protected VarInfo projection;
	protected QuadFilterPatternCanonical qfpc;

	public ConjunctiveQuery(VarInfo projection, QuadFilterPatternCanonical qfpc) {
		super();
		this.projection = projection;
		this.qfpc = qfpc;
	}

	public VarInfo getProjection() {
		return projection;
	}

	public QuadFilterPatternCanonical getPattern() {
		return qfpc;
	}

	public Op toOp() {
		Op result = qfpc.toOp();

		// Create the projection
		result = new OpProject(result, new ArrayList<>(projection.getProjectVars()));

		if(projection.getDistinctLevel() > 0) {
			result = new OpDistinct(result);
		}

		return result;
	}
}
