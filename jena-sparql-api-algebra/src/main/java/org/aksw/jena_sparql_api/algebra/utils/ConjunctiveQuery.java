package org.aksw.jena_sparql_api.algebra.utils;

import java.util.ArrayList;

import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
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


	@Override
	public String toString() {
		return "ConjunctiveQuery [projection=" + projection + ", qfpc=" + qfpc + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((projection == null) ? 0 : projection.hashCode());
		result = prime * result + ((qfpc == null) ? 0 : qfpc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConjunctiveQuery other = (ConjunctiveQuery) obj;
		if (projection == null) {
			if (other.projection != null)
				return false;
		} else if (!projection.equals(other.projection))
			return false;
		if (qfpc == null) {
			if (other.qfpc != null)
				return false;
		} else if (!qfpc.equals(other.qfpc))
			return false;
		return true;
	}


}
