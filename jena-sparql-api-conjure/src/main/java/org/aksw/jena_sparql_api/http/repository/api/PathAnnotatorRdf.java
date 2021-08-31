package org.aksw.jena_sparql_api.http.repository.api;

import java.nio.file.Path;
import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

/**
 * Interface to assign rdf metadata to paths
 *
 * @author raven
 *
 */
public interface PathAnnotatorRdf {

    /**
     * Given a possible annotation file, yield the paths it contains annotations for
     * never null
     *
     * @param path
     * @return
     */
    Collection<Path> isAnnotationFor(Path path);

    Resource getRecord(Path path);
    Resource setRecord(Path path, Resource resource);

//	default RdfFileEntity<?> getEntity(Path path) {
//		Resource info = getRecord(path);
//		RdfFileEntity<?> result = new RdfFileEntityImpl<>(this, path, Resource.class, info);
//		return result;
//	}
}
