package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

public interface ProcessSink {
	InputStream getInputStream();
	FileCreation redirectTo(Path path);
}
