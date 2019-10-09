package org.aksw.jena_sparql_api.io.endpoint;

import java.io.InputStream;


public interface FileWritingProcess {
	public void abort();

	// Probably we should not expose the path
	// Path getPath();
	
	/**
	 * 
	 * 
	 * @return
	 */
	boolean supportsConcurrentRead();
	
	/**
	 * Attempt to establish an input stream while the data is being written
	 * 
	 * New input streams will refer to the current target file of the write process.
	 * Invoking newInputStream after the completion of the process may fail if the target
	 * file was (re)moved.
	 * 
	 * Note that the location in this class must be updated if the file was moved after completion
	 * 
	 * 
	 * Note that the input stream will most likely backed by a file descriptor,
	 * which is independent of a concrete file name.
	 * 
	 * Typically, file writing processes should first create a unique
	 * temp file and only move it to
	 * 
	 * 
	 * 
	 * Only valid if supportsConcurrentRead() yields true
	 * 
	 * @return
	 */
	InputStream newInputStream();
	
	
	/**
	 * Wait for the write process to complete
	 * 
	 */
	void awaitDone();
}
