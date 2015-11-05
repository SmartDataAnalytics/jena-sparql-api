package org.aksw.jena_sparql_api.stmt;

import java.util.Comparator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.update.UpdateRequest;



/**
 * Default implementation that delegates statement parsing to
 * SparqlQueryParser and SparqlUpdateParser.
 *
 * @author raven
 *
 */
public class SparqlStmtParserImpl
    implements SparqlStmtParser
{
    private SparqlQueryParser queryParser;
    private SparqlUpdateParser updateParser;

    public SparqlStmtParserImpl(SparqlQueryParser queryParser,
            SparqlUpdateParser updateParser) {
        super();
        this.queryParser = queryParser;
        this.updateParser = updateParser;
    }

    @Override
    public SparqlStmt apply(String stmtStr) {
        SparqlStmt result;
        try {
            Query query = queryParser.apply(stmtStr);
            result = new SparqlStmtQuery(query);
        } catch(QueryParseException queryException) {

            try {
                UpdateRequest updateRequest = updateParser.apply(stmtStr);
                result = new SparqlStmtUpdate(updateRequest);

            } catch(QueryParseException updateException) {
                int delta = QueryParseExceptionComparator.doCompare(queryException, updateException);

                boolean isQueryException = delta <= 0;
                if(isQueryException) {
                    throw new RuntimeException("Failed to parse " + stmtStr, queryException);
                } else {
                    throw new RuntimeException("Failed to parse " + stmtStr, updateException);
                }
            }

        }

        return result;
    }

//    public static SparqlStmtParserImpl create() {
//        SparqlStmtParserImpl result
//    }

    public static SparqlStmtParserImpl create(SparqlParserConfig config) {
        SparqlQueryParser queryParser = SparqlQueryParserImpl.create(config);
        SparqlUpdateParser updateParser = SparqlUpdateParserImpl.create(config);
        SparqlStmtParserImpl result = new SparqlStmtParserImpl(queryParser, updateParser);

        return result;
    }

}
