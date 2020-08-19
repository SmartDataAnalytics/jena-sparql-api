package org.aksw.jena_sparql_api.io.pipe.process;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * This is just the plain command construction - it does not involve operator metadata
 * whether e.g. a file must be completely rewritten before the transformation can be applied
 *
 * @author raven
 *
 */
public interface SysCallPipeSpec {
    default String[] cmdStreamToStream() { return null; }
    default Function<Path, String[]> cmdBuilderStreamToPath() { return null; }
    default Function<Path, String[]> cmdBuilderPathToStream() { return null; }
    default Function<Path, Path> cmdBuilderPathToPath() { return null; }

    public static SysCallPipeSpec fromCmdStreamToStream(String ... cmd) {
        return new SysCallPipeSpec() {
            @Override
            public String[] cmdStreamToStream() { return cmd; }
        };
    }
}
