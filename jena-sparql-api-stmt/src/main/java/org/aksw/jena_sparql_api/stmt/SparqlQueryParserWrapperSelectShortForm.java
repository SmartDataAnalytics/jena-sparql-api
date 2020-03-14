package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;

/**
 * A wrapper for a sparql query parser that allows omitting the SELECT keyword.
 * Internally, if a parse error occurs,
 * an attempt is made to inject the SELECT-keyword at the reported error location.
 * 
 * For example, instead of having to write 'SELECT ?s { ?s ?p ?o }'
 * this wrapper allows for simply using '?s { ?s ?p ?o }'.
 * 
 * Main use case to make it more succinct to pass sparql queries as command line arguments.
 * 
 * @author raven
 *
 */
public class SparqlQueryParserWrapperSelectShortForm
	implements SparqlQueryParser
{
	protected Function<String, Query> delegate;
	
	public SparqlQueryParserWrapperSelectShortForm(Function<String, Query> delegate) {
		super();
		this.delegate = delegate;
	}
	
	@Override
	public Query apply(String str) {
		Query result = null;
		
		try {
			result = delegate.apply(str);
        } catch(QueryParseException e) {
        	int[] lineAndCol = QueryParseExceptionUtils.parseLineAndCol(e);
        	// TODO lineAndCol at present never returns null; but in principle null could be used to
        	// indicate the absence of a known position
        	if(lineAndCol != null) {
        		int line = lineAndCol[0];
        		int col = lineAndCol[1];
        		
        		int charPos = StringUtils.findCharPos(str, line, col);
        		if(charPos != -1) {
        			String inject = " SELECT ";
        			String newStr = str.substring(0, charPos) + inject + str.substring(charPos);
        			
        			try {
        				result = delegate.apply(newStr);
        			} catch(QueryParseException f) {
        				int[] lineAndCol2 = QueryParseExceptionUtils.parseLineAndCol(f);
        				if(lineAndCol2 != null) {
        					int line2 = lineAndCol2[0];
        					int col2 = lineAndCol2[1];
        				
        					int effectiveCol = line == line2 && col2 >= col + inject.length()
								? col2 - inject.length()
								: col2;

        					throw new QueryParseException(e, line, effectiveCol);
        				} else {
        					throw new QueryParseException(f, f.getLine(), f.getColumn());
        				}
        			}
        		}
        	}
        	
        	if(result == null) {
				throw new QueryParseException(e, e.getLine(), e.getColumn());        		
        	}
        }
		
		return result;
	}
	
	
	public static SparqlQueryParserWrapperSelectShortForm wrap(Function<String, Query> delegate) {
		SparqlQueryParserWrapperSelectShortForm result = new SparqlQueryParserWrapperSelectShortForm(delegate);
		return result;
	}
}
