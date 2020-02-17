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
    	SparqlStmtQuery stmt = getAsQueryStmt();
    	Query result = stmt == null ? null : stmt.getQuery();
    	return result;
    }

    default UpdateRequest getUpdateRequest() {
    	SparqlStmtUpdate stmt = getAsUpdateStmt();
    	UpdateRequest result = stmt == null ? null : stmt.getUpdateRequest();
    	return result;
    }
}
