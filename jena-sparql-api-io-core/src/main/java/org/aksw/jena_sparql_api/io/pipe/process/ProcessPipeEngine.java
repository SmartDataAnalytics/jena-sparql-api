package org.aksw.jena_sparql_api.io.pipe.process;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

/**
 * Interface to perform process execution.
 * Purpose is to abstract future process creation using NuProcess.
 *
 * @author raven
 *
 */
public interface ProcessPipeEngine {
    BiFunction<Path, Path, FileCreation> mapPathToPath(BiFunction<Path, Path, String[]> cmdBuilder);
    PathToStream mapPathToStream(Function<Path, String[]> cmdBuilder);
    StreamToStream mapStreamToStream(String[] cmd);
    BiFunction<InputStreamOrPath, Path, FileCreation> mapStreamToPath(Function<Path, String[]> cmdBuilder);
}
