package org.aksw.jena_sparql_api.stmt;

import org.aksw.jena_sparql_api.concepts.Concept;

import com.google.common.base.Function;

public interface SparqlConceptParser
    extends Function<String, Concept>
{

}
