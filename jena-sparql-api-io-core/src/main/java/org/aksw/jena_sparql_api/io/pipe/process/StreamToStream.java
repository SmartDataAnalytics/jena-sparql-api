package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.util.function.Function;

public interface StreamToStream {
    ProcessSink apply(InputStreamOrPath src);

    default Function<InputStream, InputStream> asStreamTransform() {
        return in -> apply(InputStreamOrPath.from(in)).getInputStream();
    }
}