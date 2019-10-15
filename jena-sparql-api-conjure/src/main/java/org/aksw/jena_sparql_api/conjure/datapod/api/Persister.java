package org.aksw.jena_sparql_api.conjure.datapod.api;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.Path;

/**
 * An entity that allows querying and using the persistence
 * capabilities of a datapod.
 * 
 * @author raven
 *
 */
public interface Persister {
	Resource persist(Path path);
}
