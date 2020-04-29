package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

public interface PathToStream {
    ProcessSink apply(Path src);

    default Function<Path, InputStream> asStreamSource() {
        return path -> apply(path).getInputStream();
    }
}