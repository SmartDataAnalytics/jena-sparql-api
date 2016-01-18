package org.aksw.jena_sparql_api.stmt;

public interface SparqlStmt {
    boolean isQuery();
    boolean isUpdateRequest();

    SparqlStmtUpdate getAsUpdateStmt();
    SparqlStmtQuery getAsQueryStmt();

    Exception getParseException();
    String getOriginalString();
}
