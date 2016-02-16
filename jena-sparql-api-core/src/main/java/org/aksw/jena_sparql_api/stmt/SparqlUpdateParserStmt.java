package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.update.UpdateRequest;

import com.google.common.base.Function;

/**
 * Wraps a generic statement parser as a specific update request parser
 *
 * @author raven
 *
 */
public class SparqlUpdateParserStmt
    implements SparqlUpdateParser
{
    protected Function<String, SparqlStmt> stmtParser;

    public SparqlUpdateParserStmt(Function<String, SparqlStmt> stmtParser) {
        super();
        this.stmtParser = stmtParser;
    }

    @Override
    public UpdateRequest apply(String queryStr) {
        SparqlStmt stmt = stmtParser.apply(queryStr);
        if(!stmt.isUpdateRequest()) {
            throw new RuntimeException("SPARQL statement is not a query: " + stmt);
        }

        UpdateRequest result = stmt.getAsUpdateStmt().getUpdateRequest();
        return result;
    }


    public static SparqlUpdateParserStmt wrap(Function<String, SparqlStmt> stmtParser) {
        SparqlUpdateParserStmt result = new SparqlUpdateParserStmt(stmtParser);
        return result;
    }
}
