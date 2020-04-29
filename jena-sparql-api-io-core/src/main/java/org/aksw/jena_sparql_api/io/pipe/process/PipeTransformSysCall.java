package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

public class PipeTransformSysCall
    implements PipeTransform
{
    protected SysCallPipeSpec pipeSpec;

    public PipeTransformSysCall(SysCallPipeSpec pipeSpec) {
        super();
        this.pipeSpec = pipeSpec;
    }

    @Override
    public Function<InputStream, InputStream> mapStreamToStream() {
        return ProcessPipeUtils.mapStreamToStream(pipeSpec.cmdStreamToStream())
                .asStreamTransform();
    }

    @Override
    public BiFunction<InputStream, Path, FileCreation> mapStreamToPath() {
        return (in, path) -> ProcessPipeUtils.mapStreamToPath(pipeSpec.cmdBuilderStreamToPath())
                .apply(InputStreamOrPath.from(in), path);
    }

    @Override
    public Function<Path, InputStream> mapPathToStream() {
        return ProcessPipeUtils.mapPathToStream(pipeSpec.cmdBuilderPathToStream())
                .asStreamSource();
    }


    @Override
    public BiFunction<Path, Path, FileCreation> mapPathToPath() {
        return (in, path) -> ProcessPipeUtils.mapStreamToPath(pipeSpec.cmdBuilderStreamToPath())
                .apply(InputStreamOrPath.from(in), path);
    }
}


