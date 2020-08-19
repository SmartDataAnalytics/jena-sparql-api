package org.aksw.jena_sparql_api.http.repository.api;

import java.nio.file.Path;

public interface HttpResourceRepositoryFromFileSystem
    extends HttpRepository
{
    /**
     * Obtain an entity for the given path
     *
     * The repository may consult several stores to complete this action.
     *
     * @param path
     * @return
     */
    RdfHttpEntityFile getEntityForPath(Path path);

    //RdfHttpEntityFile get(String url, String contentType, List<String> encodings) throws Exception;
}
