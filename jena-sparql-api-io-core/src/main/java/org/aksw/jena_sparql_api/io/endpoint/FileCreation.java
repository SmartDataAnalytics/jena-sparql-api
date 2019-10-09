package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * An active process that creates a file
 * 
 * @author raven
 *
 */
public interface FileCreation {
	/**
	 * A single (a better version of a future) that fires once the final file is ready
	 * Canceling the future has no upstream effect, use abort for this purpose
	 * 
	 */
	// removed
	
	/**
	 * A completable future that fires when the file creation is complete
	 * or an exception occurred
	 * 
	 * @return
	 */
	CompletableFuture<Path> future();
	//Single<Path> whenReady();
	
	/**
	 * Optional method to cancel the creation
	 * 
	 */
	void abort();
}
