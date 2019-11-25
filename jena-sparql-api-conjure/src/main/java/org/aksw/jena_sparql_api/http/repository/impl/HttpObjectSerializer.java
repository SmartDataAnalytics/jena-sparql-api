package org.aksw.jena_sparql_api.http.repository.impl;

import java.io.IOException;

import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Class to lookup java objects in order to de-serialize them and
 * to serialize them to a http store. 
 * 
 * @author raven
 *
 * @param <T>
 */
public interface HttpObjectSerializer<T> {
	HttpUriRequest createHttpRequest(String uri); // TODO Maybe it needs to be an array to cover all combinations
	RdfHttpEntityFile serialize(String uri, ResourceStore store, T data) throws IOException;
	T deserialize(RdfHttpEntityFile entity) throws IOException;
}