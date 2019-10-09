package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;

public interface FilterExecution2 {
	FilterExecution naturalDestination();

	FileDestination outputToFile(Path path);
	StreamDestination outputToStream();
}
