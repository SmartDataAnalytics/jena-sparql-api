package org.aksw.jena_sparql_api.http.repository.api;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * Repository interface to access information based on HTTP conventions, especially content negotiation
 * for a given id. The id should conform to HTTP URI specification (https://tools.ietf.org/html/rfc3986)
 * but implementations may decide to not enforce that.
 * Most prominently, this affects content type, encoding, language and charset.
 *
 * @author raven
 *
 */
public interface HttpRepository {
    RdfHttpEntityFile get(HttpRequest request, Function<HttpRequest, Entry<HttpRequest, HttpResponse>> httpRequester) throws IOException;
}
