package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

public class ResidualMatching {
	protected  Set<Set<Expr>> cnf;

	public ResidualMatching(Set<Set<Expr>> cnf) {
		super();
		this.cnf = cnf;
	}

	public Set<Set<Expr>> getCnf() {
		return cnf;
	}

	public void setCnf(Set<Set<Expr>> cnf) {
		this.cnf = cnf;
	}
	
}