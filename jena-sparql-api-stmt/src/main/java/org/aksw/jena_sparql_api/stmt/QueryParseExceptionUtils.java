package org.aksw.jena_sparql_api.stmt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.QueryParseException;

import com.google.common.collect.ComparisonChain;

public class QueryParseExceptionUtils {
	public static Pattern posPattern = Pattern.compile("line (\\d+), column (\\d+)");

	/**
	 * Replace 0 values in line and/or column with 1
	 * 
	 * @param in
	 * @return
	 */
	public static int[] adjustLineAndCol(int[] in) {
		int[] result;
		if(in != null) {
			int x = in[0];
			int y = in[1];
			result = new int[] {x == 0 ? 1 : x, y == 0 ? 1 : y};
		} else {
			result = null;
		}

		return result;
	}
	
	
	/**
	 * Parse out the line and column position from a sparql parse error message
	 * 
	 * @param str
	 * @return an array [line, col] or null if the input could not be matched
	 */
	public static int[] parseLineAndCol(String str) {
		Matcher m = posPattern.matcher(str);

		int[] result;
		if(m.find()) {
			int line = Integer.parseInt(m.group(1));
			int col = Integer.parseInt(m.group(2));
			result = new int[] {line, col};
		} else {
			result = null; // new int[] {0, 0};
		}

		return result;
	}

    public static int[] parseLineAndCol(QueryParseException e) {
    	int[] tmp = parseRawLineAndCol(e);
    	int[] result = adjustLineAndCol(tmp);
    	return result;
    }

	
    public static int[] parseRawLineAndCol(QueryParseException e) {
    	String msg = e.getMessage();
    	int[] result = parseLineAndCol(msg);
    	return result;
    }
    
    public static int[] lineAndCol(QueryParseException e) {
    	int l = e.getLine();
    	int c = e.getColumn();
    	return new int[] {l == -1 ? Integer.MAX_VALUE : l, c == -1 ? Integer.MAX_VALUE : c };
    }
    
    public static int doCompare(QueryParseException a, QueryParseException b) {
    	// A line / column value of -1 seems to indicate successful parsing,
    	// but an error in post processing,
    	// such as out of scope variables - so (-1, -1) has to be treated differently

    	// - this is inconsistent with our assumption that higher
    	// line / col numbers indicate greater progress in processing
    	// So we adjust -1 to Integer MAX_VALUE
    	
    	int[] aa = lineAndCol(a);
    	int[] bb = lineAndCol(b);
    	
    	int result = ComparisonChain.start()
    		.compare(bb[0], aa[0])
    		.compare(bb[1], aa[1])
    		.result();
    	
        return result;
    }
}
