package org.aksw.jena_sparql_api.algebra.utils;

import java.util.Set;

import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Holder for holding an immutable expression in different representations,
 * such as normal forms.
 * Lazy conversion between representations.
 *
 * @author raven
 *
 */
public class ExprHolder {
	/**
	 * The expr object to expose - may be derived from the cnf or dnf
	 */
	protected Expr expr;
    private Set<Set<Expr>> cnf;
    private Set<Set<Expr>> dnf;

    public ExprHolder(Expr expr, Set<Set<Expr>> cnf, Set<Set<Expr>> dnf) {
    	super();

    	this.expr = expr;
    	this.cnf = cnf;
    	this.dnf = dnf;
    }

    public Expr getExpr() {
    	expr = expr == null
    		? (cnf != null ? CnfUtils.toExpr(cnf) :
    		   dnf != null ? DnfUtils.toExpr(dnf) : null)
    		: expr;

    	expr = expr == null ? NodeValue.TRUE : expr;
    		   
    	return expr;
    }

    public Set<Set<Expr>> getCnf() {
    	cnf = cnf == null ? CnfUtils.toSetCnf(getExpr()) : cnf;
    	return cnf;
    }

    public Set<Set<Expr>> getDnf() {
    	dnf = dnf == null ? DnfUtils.toSetDnf(getExpr()) : dnf;
    	return dnf;
    }

	public static ExprHolder from(Expr expr) {
		return new ExprHolder(expr, null, null);
	}

	public static ExprHolder fromCnf(Set<Set<Expr>> cnf) {
		return new ExprHolder(null, cnf, null);
	}

	public static ExprHolder fromDnf(Set<Set<Expr>> dnf) {
		return new ExprHolder(null, null, dnf);
	}

	boolean isEmpty() {
		boolean result = getCnf().isEmpty();
		return result;
	}

	@Override
	public String toString() {
		String result = "" + getExpr();
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getExpr() == null) ? 0 : getExpr().hashCode());
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
		ExprHolder other = (ExprHolder) obj;
		if (getExpr() == null) {
			if (other.getExpr() != null)
				return false;
		} else if (!getExpr().equals(other.getExpr()))
			return false;
		return true;
	}
}
