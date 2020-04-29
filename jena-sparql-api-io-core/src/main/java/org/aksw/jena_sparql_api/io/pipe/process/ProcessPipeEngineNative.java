package org.aksw.jena_sparql_api.io.pipe.process;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

public class ProcessPipeEngineNative
    implements ProcessPipeEngine
{
    private static ProcessPipeEngineNative instance = null;

    public static ProcessPipeEngineNative get() {
        if(instance == null) {
            synchronized (ProcessPipeEngineNative.class) {
                if(instance == null) {
                    instance = new ProcessPipeEngineNative();
                }
            }
        }

        return instance;
    }

    @Override
    public BiFunction<Path, Path, FileCreation> mapPathToPath(BiFunction<Path, Path, String[]> cmdBuilder) {
        return ProcessPipeUtils.mapPathToPath(cmdBuilder);
    }

    @Override
    public PathToStream mapPathToStream(Function<Path, String[]> cmdBuilder) {
        return ProcessPipeUtils.mapPathToStream(cmdBuilder);
    }

    @Override
    public StreamToStream mapStreamToStream(String[] cmd) {
        return ProcessPipeUtils.mapStreamToStream(cmd);
    }

    @Override
    public BiFunction<InputStreamOrPath, Path, FileCreation> mapStreamToPath(Function<Path, String[]> cmdBuilder) {
        return ProcessPipeUtils.mapStreamToPath(cmdBuilder);
    }
}
