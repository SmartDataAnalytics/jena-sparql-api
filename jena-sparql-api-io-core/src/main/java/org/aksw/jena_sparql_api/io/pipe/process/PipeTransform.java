package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

public interface PipeTransform {
    default Function<InputStream, InputStream> mapStreamToStream() { return null; }
    default Function<Path, InputStream> mapPathToStream() { return null; }
    default BiFunction<Path, Path, FileCreation> mapPathToPath() { return null; }
    default BiFunction<InputStream, Path, FileCreation> mapStreamToPath() { return null; }
}


// Wrapper that 'fills' out missing methods from a pipe transform
class PipeTransformFiller
    implements PipeTransform
{

}


interface Pipeline
    extends AutoCloseable
{

}

class PipeComposer {
    Pipeline buildForStreamToStream() {
        return null;
    }

}



