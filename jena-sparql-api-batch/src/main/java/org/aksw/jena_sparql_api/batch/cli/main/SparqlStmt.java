package org.aksw.jena_sparql_api.batch.cli.main;

public interface SparqlStmt {
    boolean isQuery();
    boolean isUpdateRequest();

    SparqlStmtUpdate getAsUpdateStmt();
    SparqlStmtQuery getAsQueryStmt();
}
