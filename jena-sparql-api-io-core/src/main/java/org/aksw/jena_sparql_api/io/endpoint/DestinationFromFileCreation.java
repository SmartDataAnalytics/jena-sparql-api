package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

import org.aksw.commons.io.endpoint.FileCreation;

import io.reactivex.rxjava3.core.Single;

/**
 * Destination of a file that does not yet exist
 *
 * @author raven
 *
 */
public class DestinationFromFileCreation
    implements Destination
{
    protected Single<? extends FileCreation> fileCreation;

    /**
     * The disposable of the fileCreation
     */
    //protected Disposable disposable;

    protected Path fileBeingCreated;


    /**
     *
     *
     * @param fileBeingCreated
     * @param fileCreation A single representing the file creation and whose value is CACHED!
     */
    public DestinationFromFileCreation(Path fileBeingCreated, Single<? extends FileCreation> fileCreation) {
        super();
        this.fileBeingCreated = fileBeingCreated;
        this.fileCreation = fileCreation.cache();
    }

    /**
     * Cancels this destination's underlying fileCreation process and cancel it
     * Has no effect if the process already completed
     *
     */
    @Override
    public void cancelCreation() {
        throw new RuntimeException("Not implemented yet");
    }

    public Single<? extends FileCreation> getFileCreation() {
        return fileCreation;
    }

    /**
     * The file being created once there is a subscription to the single
     *
     * @return
     */
    public Path getFileBeingCreated() {
        return fileBeingCreated;
    }

    @Override
    public FilterConfig transferTo(FilterEngine engine) {
        return engine.forInput(this);
    }

    @Override
    public Single<InputStreamSupplier> prepareStream() {
        return fileCreation.flatMap(fc -> {
            Single<InputStreamSupplier> r;
            if(fc instanceof HotFile) {
                HotFile hotFile = (HotFile)fc;
                r = Single.just(InputStreamSupplierBasic.wrap((InputStreamSource)hotFile::newInputStream));//((HotFile)fc)::newInputStream;
            } else {
                r = Single.fromFuture(fc.future()).map(path -> {
                    return (InputStreamSupplier) Files.newInputStream(path, StandardOpenOption.READ);
                });
            }

            return r;
        });

    }

    @Override
    public Single<DestinationFromFile> materialize(Supplier<Path> preferredPathCallback) {
        FileCreation tmp = fileCreation.blockingGet();
        return Single.fromFuture(tmp.future())
            .map(x -> new DestinationFromFile(x));
    }

    @Override
    public String getCreationStatus() {
        // TODO Auto-generated method stub
        return null;
    }
}
