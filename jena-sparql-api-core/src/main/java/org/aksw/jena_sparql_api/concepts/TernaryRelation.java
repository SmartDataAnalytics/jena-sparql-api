package org.aksw.jena_sparql_api.concepts;

import org.apache.jena.sparql.core.Var;

public interface TernaryRelation
	extends Relation
{
	Var getS();
	Var getP();
	Var getO();
}
