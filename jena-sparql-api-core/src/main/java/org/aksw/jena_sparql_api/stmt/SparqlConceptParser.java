package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.Concept;

public interface SparqlConceptParser
    extends Function<String, Concept>
{

}
