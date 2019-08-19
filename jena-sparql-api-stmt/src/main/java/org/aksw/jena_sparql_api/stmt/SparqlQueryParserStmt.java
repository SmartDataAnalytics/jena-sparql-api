package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.apache.jena.query.Query;


/**
 * Wraps a generic statement parser as a specific query parser
 *
 * @author raven
 *
 */
public class SparqlQueryParserStmt
    implements SparqlQueryParser
{
    protected Function<String, SparqlStmt> stmtParser;

    public SparqlQueryParserStmt(Function<String, SparqlStmt> stmtParser) {
        super();
        this.stmtParser = stmtParser;
    }

    @Override
    public Query apply(String queryStr) {
        SparqlStmt stmt = stmtParser.apply(queryStr);
        if(!stmt.isQuery()) {
            throw new RuntimeException("SPARQL statement is not a query: " + stmt);
        }

        Query result = stmt.getAsQueryStmt().getQuery();
        return result;
    }


    public static SparqlQueryParserStmt wrap(Function<String, SparqlStmt> stmtParser) {
        SparqlQueryParserStmt result = new SparqlQueryParserStmt(stmtParser);
        return result;
    }
}
