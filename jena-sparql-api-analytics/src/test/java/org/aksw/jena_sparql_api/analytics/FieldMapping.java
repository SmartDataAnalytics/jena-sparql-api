package org.aksw.jena_sparql_api.analytics;

import org.aksw.jena_sparql_api.decision_tree.api.DecisionTreeSparqlExpr;
import org.apache.jena.sparql.core.Var;

public interface FieldMapping {
	Var getVar();
	DecisionTreeSparqlExpr getDefinition();
	String getDatatypeIri();
	boolean isNullable();
}
