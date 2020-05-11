package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Single;


/**
 * Destination is still a bad name, but I think it is the central entity here:
 * It is a 'IoEntityProcessingStage', so it is a factory for an IoEntity (stream / file)
 * with an associated generating workflow. In the trivial cases, destination already
 * corresponds to a workflow result - i.e. a file or a stream.
 *
 *
 *
 *
 *
 * Another attempt at defining destination:
 * A destination represents a future byte source.
 * A destination is associated with at most one generating process.
 *
 *
 *
 *
 *
 */


/**
 * Every destination must be usable as a supplier for input streams.
 *
 * Concretely, a destination can represent any of the following things:
 * (a) an anonymous source of input streams
 * (b) a file that already exists
 * (c) a file that can be generated on request
 * (d) a file under generation and will thus exist is the future
 * (e) a prior filter (for which no output type was yet requested)
 *
 *
 * In a later version we may consider replacing file with 'store' and 'reference'
 * i.e. a reference to a store that will hold the data in the future
 * But then again, this might be too much abstraction - the implication is, that
 * we would need to specify conversions between different kind of stores:
 * "ifNeedsSomeSpecificTypeOfStoreAsInput then provide a policy for converting the input data"
 * Then again, we just require the stores to be file-like, so we never have to deal with specifics
 * of different store.
 *
 * @author raven
 *
 */
public interface Destination {
    /**
     * This method requests a supplier for inputstreams.
     * For basic destinations, such as FileDestination, this process does
     * not involve any overhead.
     *
     * However, if the destination represents a workflow, prepareStream may
     * trigger a complex execution.
     * The execution may generate a file from which streams can be obtained,
     * or nothing gets executed at this point, and execution only occur when opening
     * an input stream.
     *
     *
     *
     * @return
     */
    Single<InputStreamSupplier> prepareStream();


    /**
     * Get a description of the creation status.
     *
     * @return
     */
    String getCreationStatus();

    /**
     * Materialize the destination to a given file.
     * If the destination is already a file it waits until it has been completed.
     * For non-file destinations, the callback is invoked to obtain a preferred file name
     *
     *
     * @return
     */
    Single<DestinationFromFile> materialize(Supplier<Path> preferredPathCallback) throws IOException;



    // default DestinationFromFile materializing(Supplier<Path> preferredPathCallback) throws IOException;


    // Write the file for file-based destinations
    // CompletableFuture<?> materialize();

    FilterConfig transferTo(FilterEngine engine);


    // Recursively unwind wrapped destinations
    // Use to poke through 'cancelCreation' wrappers
    // Destination unwindDestination();

    /**
     * Cancel the creation of a destination's byte source.
     * Propagates upstream unless .wrapWithoutCancel() was called
     *
     */
    default void cancelCreation() {}

    /**
     * Return a new destination whose cancelCreation method does nothing
     *
     */
    // Destination wrapWithoutCancel();
}
