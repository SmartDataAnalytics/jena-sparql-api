package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Single;

public interface FilterConfig
{
    Single<InputStreamSupplier> execStream();

    FilterConfig ifNeedsFileInput(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);
    FilterConfig ifNeedsFileOutput(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);

    FilterConfig pipeInto(FilterEngine nextFilter);


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
    DestinationFromFileCreation outputToFile(Path path);

    /**
     * If the execution requires the generation of an intermediate file, the
     * handler for ifNeedsFileOutput should be called
     *
     * @return
     */
    Destination outputToStream();


    /**
     * Method yields true if the filter needs to create an
     * intermediary output file
     *
     * @return
     */
    boolean requiresFileOutput();
    //
    //Destination naturalDestination();
    // int naturalDestination();

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
