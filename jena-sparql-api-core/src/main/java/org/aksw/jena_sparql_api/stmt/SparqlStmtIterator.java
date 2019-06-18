package org.aksw.jena_sparql_api.stmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.io.CharStreams;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.lang.arq.ParseException;

import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;

/**
 * An iterator over a string that holds a sequence of SPARQL queries; uses positions reported by parse exceptions
 * to find the boundaries of queries.
 * TODO Ideally, an extension to the jena sparql grammar should allow parsing such sequences of queries.
 * 
 * 
 * @author raven Mar 21, 2018
 *
 */
public class SparqlStmtIterator extends AbstractIterator<SparqlStmt> {

	protected Function<String, SparqlStmt> parser;

	protected String str;
	protected int line = 1;
	protected int column = 1;

	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
	public SparqlStmtIterator(Function<String, SparqlStmt> parser, String str) {
		this(parser, str, 1, 1);
	}	
	
	public SparqlStmtIterator(Function<String, SparqlStmt> parser, String str, int line, int column) {
		super();
		this.parser = parser;
		this.str = str;
		this.line = line;
		this.column = column;
	}


	public static int toCharPos(String str, int lineNumber, int columnNumber) {
		BufferedReader br = new BufferedReader(new StringReader(str));

		int lineIndex = Math.max(0, lineNumber - 1);
		int columnIndex = Math.max(0, columnNumber - 1);

		int result = 0;
		for (int i = 0; i < lineIndex; ++i) {
			String l;
			try {
				l = br.readLine();
			} catch (IOException e) {
				// Should never happen
				throw new RuntimeException(e);
			}
			result = result + l.length() + 1; // +1 -> the newline character
		}

		result += columnIndex;

		return result;
	}

	public static boolean isEmptyString(String str) {
        return str == null ? true : str.trim().isEmpty();
	}

	// public static raiseException(QueryParseException ex) {
	//
	// }

	public static Pattern posPattern = Pattern.compile("line (\\d+), column (\\d+)");

	public static int[] parsePos(String str) {
		Matcher m = posPattern.matcher(str);

		int[] result = m.find() ? new int[] { Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)) }
				: new int[] { 0, 0 };

		return result;
	}

	@Override
	protected SparqlStmt computeNext() {
		if (isEmptyString(str)) {
			return endOfData();
		}

		SparqlStmt result = parser.apply(str);

		// Get the string up to the point where a parse error was encountered
		QueryParseException ex = result.getParseException();
		if (ex != null) {
			int[] exPos = parsePos(ex.getMessage());

			int pos = toCharPos(str, exPos[0], exPos[1]);

			line = line + Math.max(0, exPos[0] - 1);
			column = column + Math.max(0, exPos[1] - 1);

			String retryStr;
			try {
				retryStr = str.substring(0, pos);
			} catch(StringIndexOutOfBoundsException e) {
				throw new RuntimeException("Error near line " + line + ", column " + column + ".", ex);
			}

			// Note: Jena parses an empty string as a sparql update statement without errors
			if (isEmptyString(retryStr)) {
				throw new RuntimeException("Error near line " + line + ", column " + column + ".", ex);
			}

			result = parser.apply(retryStr);

			QueryParseException retryEx = result.getParseException();
			if (retryEx != null) {
				throw new RuntimeException("Error near line " + line + ", column " + column + ".", retryEx);
			}

			str = str.substring(pos);
		} else {
			// TODO Move position to last char in the string
			str = "";
		}

		return result;
	}

}
