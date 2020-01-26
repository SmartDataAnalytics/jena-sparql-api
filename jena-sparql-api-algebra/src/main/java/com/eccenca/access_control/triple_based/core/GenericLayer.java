package com.eccenca.access_control.triple_based.core;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.XExpr;
import org.aksw.jena_sparql_api.utils.ValueSetOld;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class GenericLayer {
	protected List<Var> vars;
	protected Relation relation;
	protected XExpr expr;
	protected ValueSetOld<Binding> valueSet;
	
	public GenericLayer(Relation relation) {
		this.relation = relation;
	}

	public List<Var> getVars() {
		return vars;
	}
	public void setVars(List<Var> vars) {
		this.vars = vars;
	}
	public Relation getRelation() {
		return relation;
	}
	public void setRelation(Relation relation) {
		this.relation = relation;
	}
	public XExpr getExpr() {
		return expr;
	}
	public void setExpr(XExpr expr) {
		this.expr = expr;
	}
	public ValueSetOld<Binding> getValueSet() {
		return valueSet;
	}
	public void setValueSet(ValueSetOld<Binding> valueSet) {
		this.valueSet = valueSet;
	}

	public static GenericLayer create(Relation relation) {
		return new GenericLayer(relation);
	}
	
	
}
