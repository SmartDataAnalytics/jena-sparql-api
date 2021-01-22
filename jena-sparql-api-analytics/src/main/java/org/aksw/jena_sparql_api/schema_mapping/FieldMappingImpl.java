package org.aksw.jena_sparql_api.schema_mapping;

import java.io.Serializable;

import org.aksw.jena_sparql_api.decision_tree.api.DecisionTreeSparqlExpr;
import org.apache.jena.sparql.core.Var;

public class FieldMappingImpl
	implements FieldMapping, Serializable
{
	private static final long serialVersionUID = -7307387469369671612L;

	protected Var var;
	protected DecisionTreeSparqlExpr definition;
	protected String datataypeIri;
	protected boolean isNullable;
	
	public FieldMappingImpl(Var var, DecisionTreeSparqlExpr definition, String datataypeIri, boolean isNullable) {
		super();
		this.var = var;
		this.definition = definition;
		this.datataypeIri = datataypeIri;
		this.isNullable = isNullable;
	}

	@Override
	public Var getVar() {
		return var;
	}

	@Override
	public DecisionTreeSparqlExpr getDefinition() {
		return definition;
	}

	@Override
	public String getDatatypeIri() {
		return datataypeIri;
	}

	@Override
	public boolean isNullable() {
		return isNullable;
	}

	@Override
	public String toString() {
		return "[tgtVar=" + var + ", datataypeIri=" + datataypeIri + ", isNullable=" + isNullable + ", definition=\n" + definition + "]";
	}
	
}
