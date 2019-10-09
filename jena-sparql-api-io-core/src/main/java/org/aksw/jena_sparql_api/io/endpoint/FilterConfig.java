package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface FilterConfig
	extends InputStreamSupplier
{
	FilterConfig ifNeedsFileInput(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);
	FilterConfig ifNeedsFileOutput(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);

	/**
	 * 
	 * 
	 * If explicit output to a file is requested, the handler set via
	 * ifNeedsFileOutput will not be called.
	 * 
	 * 
	 * @param path
	 * @return
	 */
	FilterExecution outputToFile(Path path);
	
	/**
	 * If the execution requires the generation of an intermediate file, the
	 * handler for ifNeedsFileOutput should be called
	 * 
	 * @return
	 */
	FilterExecution outputToStream();
	
	int naturalDestination();
	// lazy destination
	//Endpoint naturalDestination();
	
//	FileWritingProcess execToFile(Path path) throws IOException;
	//InputStream execStream() throws IOException;
		
	/**
	 * If the filter needs to create a temporary file in order to serve the stream,
	 * then the first argument callback can be used to provide a desired location.
	 * 
	 * This is useful in conjunction with caching of artifacts:
	 * If streaming directly is not possible because an
	 * intermediary file has to be created anyway, then allow the application
	 * to e.g. organize it into some approriate directory.
	 * 
	 * Note, execToFile will always create the file.
	 * This method provides callbacks for abstracting non-streaming transformers
	 * 
	 * 
	 * @param intermediateFileCallback
	 * @return
	 */
//	InputStream execStream(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback) throws IOException;
}
