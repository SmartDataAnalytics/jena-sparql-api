package org.aksw.jena_sparql_api.http.repository.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public interface HttpResourceRepositoryFromFileSystem {
	/**
	 * Obtain an entity for the given path
	 * 
	 * The repository may consult several stores to complete this action.
	 * 
	 * @param path
	 * @return
	 */
	RdfHttpEntityFile getEntityForPath(Path path);

	
	RdfHttpEntityFile get(HttpRequest request, Function<HttpRequest, Entry<HttpRequest, HttpResponse>> httpRequester) throws IOException;

	//RdfHttpEntityFile get(String url, String contentType, List<String> encodings) throws Exception;
}
