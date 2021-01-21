package org.aksw.jena_sparql_api.decision_tree.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.decision_tree.impl.jena.ConditionalVarDefinition;
import org.apache.jena.sparql.core.Var;

/**
 * Var definitions based on decision tree structures.
 * This allows for the use of 'discriminator' columns, such is
 * ?x != (if (?p = 1) then IRI(STR(?o)) if (?p = 0) then ?o) 
 * 
 * 
 * @author raven
 *
 */
public class ConditionalVarDefinitionImpl
	implements ConditionalVarDefinition
{
	protected Map<Var, DecisionTreeSparqlExpr> definitions;
	
	public ConditionalVarDefinitionImpl() {
		this(new LinkedHashMap<>());
	}

	public ConditionalVarDefinitionImpl(Map<Var, DecisionTreeSparqlExpr> definitions) {
		super();
		this.definitions = definitions;
	}
	
	@Override
	public ConditionalVarDefinitionImpl put(Var var, DecisionTreeSparqlExpr definition) {
		definitions.put(var, definition);
		return this;
	}
	
	@Override
	public Map<Var, DecisionTreeSparqlExpr> getDefinitions() {
		return definitions;
	}

	@Override
	public String toString() {
		return "ConditionalVarDefinitionImpl [definitions=" + definitions + "]";
	}
}