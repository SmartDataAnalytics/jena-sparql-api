package org.aksw.jena_sparql_api.decision_tree.api;

import java.util.LinkedHashMap;
import java.util.Map;

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
public class ConditionalVarDefinitionImpl {
	protected Map<Var, DecisionTreeSparqlExpr> definitions;
	
	public ConditionalVarDefinitionImpl() {
		this(new LinkedHashMap<>());
	}

	public ConditionalVarDefinitionImpl(Map<Var, DecisionTreeSparqlExpr> definitions) {
		super();
		this.definitions = definitions;
	}
	
	public ConditionalVarDefinitionImpl put(Var var, DecisionTreeSparqlExpr definition) {
		definitions.put(var, definition);
		return this;
	}
	
}