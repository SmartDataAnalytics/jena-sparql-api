package org.aksw.jena_sparql_api.http.repository.api;

import java.nio.file.Path;
import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

/**
 * A resource is identified by an ID and can host multiple content entities.
 *
 * @author raven
 */
public interface RdfHttpResourceFile {

    ResourceStore getResourceStore();
    Collection<RdfHttpEntityFile> getEntities();

    /** The relative path that backs this http resource */
    Path getRelativePath();

    /** Return a http entity with a certain path relative to {{@link #getRelativePath()}.
        TODO Clarify whether the path must not resolve to a ancestor folder of this entity's path */
    RdfHttpResourceFile resolve(String path);

    default Path getAbsolutePath() {
        Path relPath = getRelativePath();
        Path parentAbsPath = getResourceStore().getAbsolutePath();
        Path result = parentAbsPath.resolve(relPath);

        return result;
    }


    /**
     * Get or create an entity that matches the given description.
     * This is typically done based on an RDF description corresponding to HTTP accept headers:
     * accept, accept-encoding, accept-charset, accept-language.
     * However, the design intentionally allows for custom resolution mechanisms.
     *
     * TODO Add the RFC number(s) the resolution mechanism should adhere to
     *
     *
     * @param description
     * @return
     */
    RdfHttpEntityFile allocate(Resource description);
}
