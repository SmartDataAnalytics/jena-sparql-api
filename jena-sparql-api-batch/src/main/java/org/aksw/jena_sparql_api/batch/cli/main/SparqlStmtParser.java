package org.aksw.jena_sparql_api.batch.cli.main;

import com.google.common.base.Function;

public interface SparqlStmtParser
    extends Function<String, SparqlStmt>
{
}
