package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import com.google.common.io.ByteSource;

import io.reactivex.rxjava3.core.Single;

public class DestinationFromByteSource
    implements Destination
{
    protected ByteSource byteSource;

    public DestinationFromByteSource(ByteSource byteSource) {
        super();
        this.byteSource = byteSource;
    }

    @Override
    public Single<InputStreamSupplier> prepareStream() {
        return Single.just(InputStreamSupplierBasic.wrap(byteSource::openStream));
    }

    @Override
    public FilterConfig transferTo(FilterEngine engine) {
        return engine.forInput(InputStreamSupplierBasic.wrap(byteSource::openStream));
    }

    @Override
    public Single<DestinationFromFile> materialize(Supplier<Path> preferredPathCallback) throws IOException {
        Path path = preferredPathCallback.get();
        if(path == null) {
            path = Files.createTempFile("tmp", "suffix");
        }

        Files.copy(byteSource.openStream(), path);
        // TODO copy
        return Single.just(new DestinationFromFile(path));
    }

    @Override
    public String getCreationStatus() {
        // TODO Auto-generated method stub
        return null;
    }

}
