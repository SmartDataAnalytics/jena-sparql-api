package org.aksw.jena_sparql_api.stmt;

import java.io.Serializable;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.update.UpdateRequest;

/**
 * SparqlStmt is a unified interface for query and update statements.
 * For cases where a stmt does not fall into these two categories,
 * a special {@link SparqlStmtUnknown} exists.
 *
 * SparqlStmt's are default serializable: Only the string form is serialized
 * and upon deserialization an attempt with the default parser is made to restore the statement.
 *
 * Note, that exceptions are thus not actually serialized - instead an attempt is made to recreate them.
 * While typically this yields the same exception type, the orginal stack trace will be lost.
 *
 *
 * @author raven
 *
 */
public interface SparqlStmt
    extends Serializable
{
    boolean isQuery();
    boolean isUpdateRequest();

    boolean isUnknown();

    boolean isParsed();

    SparqlStmtUpdate getAsUpdateStmt();
    SparqlStmtQuery getAsQueryStmt();

    QueryParseException getParseException();
    String getOriginalString();

    SparqlStmt clone();

    /**
     * Return the prefix mapping of the query or update request.
     * Only valid if isParsed() is true.
     *
     * @return the prefix mapping
     */
    PrefixMapping getPrefixMapping();

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
