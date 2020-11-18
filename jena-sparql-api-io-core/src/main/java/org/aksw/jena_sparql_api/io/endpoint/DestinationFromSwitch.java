package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Single;

/**
 * Switching destination
 * If a file for a DestinationFromFileCreation finishes, it can replace itself with
 * a DestinationFromFile
 *
 * Right now I am not sure whether this is useful: If a stream is requested, it has to
 * do all the preparation work anyway
 *
 * If we think of a source + filter as a virtual source, then it is actually ok:
 * Let's not forget that a destination is a supplier for input streams
 * Repeatedly executing such a workflow may re-use prior generated files instead of
 * repeating the process again from the source file
 *
 * However, the purpose of this framework is not to do full caching, but only
 * abstract java and system filters, but allow controlling what should happen with the files
 * in case they get generated.
 *
 * The next level of abstraction could be thought of as treating files as stores: E.g. writing a stream to HDFS
 * and passing a reference to it to a system command
 *
 *
 * @author raven
 *
 */
public class DestinationFromSwitch
    implements Destination
{
    protected Destination destination;

    public Destination getDestination() {
        return destination;
    }

    @Override
    public Single<InputStreamSupplier> prepareStream() {
        Destination tmp = getDestination();
        Single<InputStreamSupplier> result = tmp.prepareStream();
        return result;
    }

    @Override
    public FilterConfig transferTo(FilterEngine engine) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Single<DestinationFromFile> materialize(Supplier<Path> preferredPathCallback) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCreationStatus() {
        // TODO Auto-generated method stub
        return null;
    }
}
