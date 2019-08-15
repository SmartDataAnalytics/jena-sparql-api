package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.QueryParseException;

public interface SparqlStmt {
    boolean isQuery();
    boolean isUpdateRequest();

    boolean isParsed();
    
    SparqlStmtUpdate getAsUpdateStmt();
    SparqlStmtQuery getAsQueryStmt();

    QueryParseException getParseException();
    String getOriginalString();
}
