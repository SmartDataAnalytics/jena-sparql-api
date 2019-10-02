package org.aksw.jena_sparql_api.http.repository.api;

import java.nio.file.Path;

/**
 * Supplier of paths for hashes
 * The base path should be an absolute path
 * 
 * @author raven
 *
 */
public interface HashSpace {
	Path get(String hash);
}