package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.io.filter.sys.FilterExecutionFromSysFunction;

import io.reactivex.rxjava3.core.Single;

public class FilterExecutionJava
    implements FilterConfig
{
    protected Function<InputStream, InputStream> processor;
    //protected Single<InputStreamSupplier> inputStreamSupplier;
    protected Destination source;

    public FilterExecutionJava(Function<InputStream, InputStream> processor, Destination source) {//, Single<InputStreamSupplier> inputStreamSupplier) {
        this.processor = Objects.requireNonNull(processor);
        //this.inputStreamSupplier = Objects.requireNonNull(inputStreamSupplier);
        this.source = source;
    }

//	@Override
//	public FileWritingProcess execToFile(Path path) throws IOException {
//		ConcurrentFileEndpoint out = ConcurrentFileEndpoint.create(path, StandardOpenOption.CREATE);
//		try(InputStream in = inputStreamSupplier.execStream()) {
//			ByteStreams.copy(in, Channels.newOutputStream(out));
//		}
//
//		return null;
//	}

    /**
     * Ideally, premature closing of the input stream should
     * propagate upstream and terminate any transformation processes upstream
     *
     */
    @Override
    public Single<InputStreamSupplier> execStream() {
        return source.prepareStream().map(inSupp -> {
            return () -> {
                Single<InputStream> r = inSupp.execStream().map(processor::apply);
                //InputStream r = processor.apply(in);
                return r;
            };
        });
    }

    /**
     * Same as execStream() as this
     * execution engine will never ask for the creation of temporary files
     * @throws IOException
     *
     */
//	@Override
//	public InputStream execStream(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback) throws IOException {
//		InputStream result = execStream();
//		return result;
//	}

    @Override
    public FilterConfig ifNeedsFileInput(Supplier<Path> pathRequester,
            BiConsumer<Path, FileWritingProcess> processCallback) {
        return this;
    }

    @Override
    public FilterConfig ifNeedsFileOutput(Supplier<Path> pathRequester,
            BiConsumer<Path, FileWritingProcess> processCallback) {
        return this;
    }

    @Override
    public FilterConfig pipeInto(FilterEngine nextFilter) {
        FilterConfig result = nextFilter.forInput(this);
        return result;
    }

    @Override
    public DestinationFromFileCreation outputToFile(Path path) {
        return new DestinationFromFileCreation(path,
                FilterExecutionFromSysFunction.forceDestinationToFile(execStream(), path));
    }

    @Override
    public Destination outputToStream() {
        return new DestinationFilter(this);
    }

    @Override
    public boolean requiresFileOutput() {
        return false;
    }

    @Override
    public String toString() {
        return "FilterExecutionJava [processor=" + processor + ", source=" + source + "]";
    }

}
