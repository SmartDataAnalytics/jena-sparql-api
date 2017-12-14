package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.BiMap;

public class ResidualMatching {
	protected BiMap<Node, Node> iso;
	
	protected Set<Set<Expr>> cnf;
	
	// TODO Maybe this should be residualOp (which can be cast to a quad pattern?)
	protected Set<Quad> residualQuadJoins; 

	public ResidualMatching(boolean tf) {
		this(tf == true ? Collections.emptySet() : Collections.singleton(Collections.emptySet()));
	}
	
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

	public BiMap<Node, Node> getIso() {
		return iso;
	}

	public void setIso(BiMap<Node, Node> iso) {
		this.iso = iso;
	}

	
	
	public Set<Quad> getResidualQuadJoins() {
		return residualQuadJoins;
	}

	public ResidualMatching setResidualQuadJoins(Set<Quad> residualQuadJoins) {
		this.residualQuadJoins = residualQuadJoins;
		return this;
	}
}

