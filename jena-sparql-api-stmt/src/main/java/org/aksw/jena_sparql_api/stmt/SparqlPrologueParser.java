package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.apache.jena.sparql.core.Prologue;

public interface SparqlPrologueParser
    extends Function<String, Prologue>
{

}
