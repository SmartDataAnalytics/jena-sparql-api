package org.aksw.jena_sparql_api.stmt;

import java.util.Comparator;
import java.util.Map.Entry;

import org.apache.jena.query.QueryParseException;

import com.github.jsonldjava.shaded.com.google.common.collect.Maps;
import com.google.common.collect.ComparisonChain;

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

    public static Entry<Integer, Integer> lineAndCol(QueryParseException e) {
    	int l = e.getLine();
    	int c = e.getColumn();
    	return Maps.immutableEntry(l == -1 ? Integer.MAX_VALUE : l, c == -1 ? Integer.MAX_VALUE : c);
    }
    
    public static int doCompare(QueryParseException a, QueryParseException b) {
    	// A line / column value of -1 seems to indicate successful parsing,
    	// but an error in post processing,
    	// such as out of scope variables - so (-1, -1) has to be treated differently

    	// - this is inconsistent with our assumption that higher
    	// line / col numbers indicate greater progress in processing
    	// So we adjust -1 to Integer MAX_VALUE
    	
    	Entry<Integer, Integer> aa = lineAndCol(a);
    	Entry<Integer, Integer> bb = lineAndCol(b);
    	
    	int result = ComparisonChain.start()
    		.compare(bb.getKey(), aa.getKey())
    		.compare(bb.getValue(), aa.getValue())
    		.result();
    	
        return result;
    }
}