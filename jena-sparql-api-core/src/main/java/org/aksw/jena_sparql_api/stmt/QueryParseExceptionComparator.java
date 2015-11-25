package org.aksw.jena_sparql_api.stmt;

import java.util.Comparator;

import com.hp.hpl.jena.query.QueryParseException;

/**
 * Compares QueryParseExceptions by their line and column number.
 * If multiple parsers attempt to parse a sparql statement,
 * this comparator can be used to detect which parser came farthest.
 *
 * @author raven
 *
 */
public class QueryParseExceptionComparator
    implements Comparator<QueryParseException>
{

    @Override
    public int compare(QueryParseException a, QueryParseException b) {
        int result = doCompare(a, b);
        return result;
    }

    public static int doCompare(QueryParseException a, QueryParseException b) {
        int result = b.getLine() - a.getLine();

        result = result == 0 ? result : b.getColumn() - a.getColumn();
        return result;
    }
}