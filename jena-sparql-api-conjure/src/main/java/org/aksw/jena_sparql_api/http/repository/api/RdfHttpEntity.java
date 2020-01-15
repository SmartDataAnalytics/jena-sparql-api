package org.aksw.jena_sparql_api.http.repository.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Resource;

public interface RdfHttpEntity {
	Resource getCombinedInfo();

	InputStream open() throws IOException;
}
