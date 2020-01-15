package org.aksw.jena_sparql_api.io.endpoint;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface FilterBuilder {
	// setInput(File file);
	
	Destination getInput();
	void setInputFileCreationHandler(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);

	// Invoked if the filter cannot handle streams
	void setOutputFileCreationHandler(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);

	Destination getOutput();
}
