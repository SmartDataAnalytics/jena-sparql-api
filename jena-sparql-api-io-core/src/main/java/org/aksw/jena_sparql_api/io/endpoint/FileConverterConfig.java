package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface FileConverterConfig {
	FileConverterConfig obtainPathFrom(Supplier<Path> path);
	
	
}
