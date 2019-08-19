package org.aksw.jena_sparql_api.stmt;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;

import com.google.common.base.Function;

public interface SparqlRelationParser
    extends Function<String, BinaryRelation>
{

}
