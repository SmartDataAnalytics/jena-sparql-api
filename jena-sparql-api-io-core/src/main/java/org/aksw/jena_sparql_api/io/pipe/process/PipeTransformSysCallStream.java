package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

/**
 * Implementation of a pipe transform that pipes all
 * arguments to a streaming syscall
 *
 * @author raven
 *
 */
public class PipeTransformSysCallStream
    implements PipeTransform
{
    protected String[] cmd;
    protected ProcessPipeEngine engine;

    public PipeTransformSysCallStream(String[] cmd) {
        this(cmd, ProcessPipeEngineNative.get());
    }

    public PipeTransformSysCallStream(String[] cmd, ProcessPipeEngine engine) {
        super();
        this.cmd = cmd;
        this.engine = engine;
    }

    @Override
    public Function<InputStream, InputStream> mapStreamToStream() {
        return engine.mapStreamToStream(cmd)
                .asStreamTransform();
    }

    @Override
    public BiFunction<InputStream, Path, FileCreation> mapStreamToPath() {
        return (in, path) -> engine.mapStreamToStream(cmd)
                .apply(InputStreamOrPath.from(in)).redirectTo(path);
    }

    @Override
    public Function<Path, InputStream> mapPathToStream() {
        return path -> engine.mapStreamToStream(cmd)
                    .apply(InputStreamOrPath.from(path)).getInputStream();
    }

    @Override
    public BiFunction<Path, Path, FileCreation> mapPathToPath() {
        return (src, tgt) -> engine.mapStreamToStream(cmd)
                    .apply(InputStreamOrPath.from(src)).redirectTo(tgt);
    }
}
