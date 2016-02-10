package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.update.UpdateRequest;



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
    protected SparqlQueryParser queryParser;
    protected SparqlUpdateParser updateParser;

    /**
     * If true, parsing will never throw an exception. Instead, the returned
     * stmt's .isParsed() method will return null, and the original queryString / updateString can be obtained.
     */
    protected boolean actAsClassifier;

    public SparqlStmtParserImpl(SparqlQueryParser queryParser,
            SparqlUpdateParser updateParser) {
        this(queryParser, updateParser, false);
    }

    public SparqlStmtParserImpl(SparqlQueryParser queryParser,
            SparqlUpdateParser updateParser, boolean actAsClassifier) {
        super();
        this.queryParser = queryParser;
        this.updateParser = updateParser;
        this.actAsClassifier = actAsClassifier;
    }

    public SparqlQueryParser getQueryParser() {
        return queryParser;
    }

    public SparqlUpdateParser getUpdateParser() {
        return updateParser;
    }

    public boolean isActAsClassifier() {
        return actAsClassifier;
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
                    if(actAsClassifier) {
                        result = new SparqlStmtQuery(stmtStr, queryException);
                    } else {
                        throw new RuntimeException("Failed to parse " + stmtStr, queryException);
                    }
                } else {
                    if(actAsClassifier) {
                        result = new SparqlStmtUpdate(stmtStr, updateException);
                    } else {
                        throw new RuntimeException("Failed to parse " + stmtStr, updateException);
                    }
                }
            }

        }

        return result;
    }

//    public static SparqlStmtParserImpl create() {
//        SparqlStmtParserImpl result
//    }

    public static SparqlStmtParserImpl create(SparqlParserConfig config) {
        SparqlStmtParserImpl result = create(config, false);
        return result;
    }

    public static SparqlStmtParserImpl create(SparqlParserConfig config, boolean actAsClassifier) {
        SparqlQueryParser queryParser = SparqlQueryParserImpl.create(config);
        SparqlUpdateParser updateParser = SparqlUpdateParserImpl.create(config);
        SparqlStmtParserImpl result = new SparqlStmtParserImpl(queryParser, updateParser, actAsClassifier);

        return result;
    }

}
