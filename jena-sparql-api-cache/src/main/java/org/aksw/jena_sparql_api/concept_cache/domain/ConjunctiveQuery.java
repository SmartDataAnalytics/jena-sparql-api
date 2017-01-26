package org.aksw.jena_sparql_api.concept_cache.domain;

import org.aksw.jena_sparql_api.concept_cache.core.VarInfo;

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
}
