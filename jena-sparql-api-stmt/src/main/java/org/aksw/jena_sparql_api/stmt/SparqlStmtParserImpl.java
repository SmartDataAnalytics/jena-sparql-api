package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
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
                int delta = QueryParseExceptionUtils.doCompare(queryException, updateException);

                boolean isQueryException = delta <= 0;
                if(isQueryException) {
                    if(actAsClassifier) {
                        result = new SparqlStmtQuery(stmtStr, queryException);
                    } else {
                        throw new QueryParseException("Failed to parse " + stmtStr, queryException, queryException.getLine(), queryException.getColumn());
                    }
                } else {
                    if(actAsClassifier) {
                        result = new SparqlStmtUpdate(stmtStr, updateException);
                    } else {
                        throw new QueryParseException("Failed to parse " + stmtStr, updateException, updateException.getLine(), updateException.getColumn());
                    }
                }
            } catch(Exception e) {
                if(actAsClassifier) {
                    result = new SparqlStmtUnknown(stmtStr, new QueryParseException(e, 1, 1));
                } else {
                    throw new QueryParseException("Failed to parse " + stmtStr, e, 1, 1);
                }
            }

        } catch(Exception e) {
            if(actAsClassifier) {
                result = new SparqlStmtUnknown(stmtStr, new QueryParseException(e, 1, 1));
            } else {
                throw new QueryParseException("Failed to parse " + stmtStr, e, 1, 1);
            }
        }

        return result;
    }

//    public static SparqlStmtParserImpl create() {
//        SparqlStmtParserImpl result
//    }
    /**
     * Create an ARQ-based parser that throws exceptions on parse errors
     * @return
     */
    public static SparqlStmtParserImpl create() {
        SparqlStmtParserImpl result = create(Syntax.syntaxARQ, false);
        return result;
    }

    public static SparqlStmtParserImpl create(PrefixMapping prefixMapping) {
        SparqlStmtParserImpl result = create(Syntax.syntaxARQ, prefixMapping, false);
        return result;
    }

    public static SparqlStmtParserImpl create(Syntax syntax, boolean actAsClassifier) {
        SparqlStmtParserImpl result = create(SparqlParserConfig.newInstance()
                .setSyntax(syntax).applyDefaults(), actAsClassifier);
        return result;
    }

    public static SparqlStmtParserImpl create(Syntax syntax, PrefixMapping prefixMapping, boolean actAsClassifier) {
        SparqlStmtParserImpl result = create(SparqlParserConfig.newInstance().setSyntax(syntax).setPrefixMapping(prefixMapping).applyDefaults(), actAsClassifier);
        return result;
    }

    public static SparqlStmtParserImpl create(Syntax syntax, PrefixMapping prefixMapping, PrefixMapping sharedPrefixes, boolean actAsClassifier) {
        SparqlStmtParserImpl result = create(SparqlParserConfig.newInstance()
                .setSyntax(syntax).setPrefixMapping(prefixMapping)
                .setSharedPrefixes(sharedPrefixes).applyDefaults(), actAsClassifier);
        return result;
    }

    public static SparqlStmtParserImpl create(Syntax syntax, Prologue prologue, boolean actAsClassifier) {
        SparqlStmtParserImpl result = create(SparqlParserConfig.newInstance().setSyntax(syntax).setPrologue(prologue).applyDefaults(), actAsClassifier);
        return result;
    }

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

    public static SparqlStmtParserImpl createAsGiven() {
        return create(SparqlParserConfig.newInstance().parseAsGiven().applyDefaults());
    }

    public static SparqlStmtParserImpl createAsGiven(boolean actAsClassifier) {
        return create(SparqlParserConfig.newInstance().parseAsGiven().applyDefaults(), actAsClassifier);
    }

}
