package org.aksw.jena_sparql_api.io.endpoint;

/**
 * A destination represents
 * (something at the end of the stream, that can also be used as a source) 
 * Conceptually this is still broken - an endpoint is both source and sink
 * but if the target is a stream, then its not a source
 * 
 * @author raven
 *
 */
public interface FilterExecution {
	
	
	//boolean isFileDestination();
	
	FilterConfig transferTo(FilterEngine engine);

	FileWritingProcess create();

	
	
	
}
