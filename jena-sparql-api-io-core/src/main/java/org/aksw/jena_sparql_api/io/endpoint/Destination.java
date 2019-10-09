package org.aksw.jena_sparql_api.io.endpoint;

import io.reactivex.Single;

/**
 * Every destination must be usable as a supplier for input streams.
 * 
 * Concretely, a destination can represent any of the following things:
 * (a) an anonymous source of input streams
 * (b) a file that already exists
 * (c) a file that can be generated on request
 * (d) a file under generation and will thus exist is the future
 * (e) a prior filter (for which no output type was yet requested)
 * 
 * In a later version we may consider replacing file with 'store' and 'reference'
 * i.e. a reference to a store that will hold the data in the future
 * But then again, this might be too much abstraction - the implication is, that
 * we would need to specify conversions between different kind of stores:
 * "ifNeedsSomeSpecificTypeOfStoreAsInput then provide a policy for converting the input data"
 * Then again, we just require the stores to be file-like, so we never have to deal with specifics
 * of different store.
 * 
 * @author raven
 *
 */
public interface Destination {	
	Single<InputStreamSupplier> prepareStream();   

	// Write the file for file-based destinations
	// CompletableFuture<?> materialize();
	
	FilterConfig transferTo(FilterEngine engine);


	//boolean isFileDestination();
}
