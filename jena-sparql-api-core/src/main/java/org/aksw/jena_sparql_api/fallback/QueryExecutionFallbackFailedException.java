/**
 * 
 */
package org.aksw.jena_sparql_api.fallback;

/**
 * @author Lorenz Buehmann
 *
 */
public class QueryExecutionFallbackFailedException extends RuntimeException{
	
	private static final String MESSAGE = "None of the query executions terminated successfully.";
	
	public QueryExecutionFallbackFailedException() {
		super(MESSAGE);
	}
}
