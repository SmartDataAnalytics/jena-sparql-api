package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.update.UpdateRequest;

public interface SparqlStmt {
    boolean isQuery();
    boolean isUpdateRequest();

    boolean isParsed();
    
    SparqlStmtUpdate getAsUpdateStmt();
    SparqlStmtQuery getAsQueryStmt();

    QueryParseException getParseException();
    String getOriginalString();
    
    
    default Query getQuery() {
    	return getAsQueryStmt().getQuery();
    }

    default UpdateRequest getUpdateRequest() {
    	return getAsUpdateStmt().getUpdateRequest();
    }

}
