package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

public interface SparqlStmtParser
    extends Function<String, SparqlStmt>
{
}
