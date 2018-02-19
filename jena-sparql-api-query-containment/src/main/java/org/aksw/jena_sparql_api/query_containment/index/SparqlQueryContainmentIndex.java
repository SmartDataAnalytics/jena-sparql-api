package org.aksw.jena_sparql_api.query_containment.index;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

public interface SparqlQueryContainmentIndex<K, R>
	extends QueryContainmentIndex<K, Var, Op, R, SparqlTreeMapping<R>>
{
}
