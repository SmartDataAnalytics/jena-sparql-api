package org.aksw.jena_sparql_api.io.endpoint;

import java.io.InputStream;
import java.nio.file.Path;

import io.reactivex.Single;

/**
 * An engine is a factory for executions that process input to output.
 *
 * Note that executions are lazy - no processing takes place until the
 * result is requested.
 * 
 * @author raven
 *
 */
public interface FilterEngine {
	FilterConfig forInput(Path in);
	FilterConfig forInput(InputStreamSupplier in);
	
	/**
	 * Create an input from a file that is currently written to.
	 * If the engine needs the file, it can wait for it to become ready.
	 * otherwise, it can obtain a live-stream with the data
	 * 
	 * 
	 * @param futurePath
	 * @return
	 */
	FilterConfig forInput(Single<Path> futurePath);

	/**
	 * Ideally input should be source-like:
	 * sources are idle entities (do not need to be closed by the engine) 
	 * that allow for creation of as many input
	 * streams as desired.
	 * 
	 * But wrapping an existing input stream as a source seems useful
	 * 
	 * @param in
	 * @return
	 */
	default FilterConfig forInput(InputStream in) {
		FilterConfig result = forInput(() -> in);
		return result;
	}

}
