package org.aksw.jena_sparql_api.io.endpoint;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

import io.reactivex.rxjava3.core.Single;

public class FilterEngineJava
    implements FilterEngine
{
    protected Function<InputStream, InputStream> processor;

    public FilterEngineJava(Function<InputStream, InputStream> processor) {
        super();
        this.processor = processor;
    }

    @Override
    public FilterConfig forInput(Path in) {
        return new FilterExecutionJava(processor, Destinations.fromFile(in));
    }

    @Override
    public FilterConfig forInput(InputStreamSupplier in) {
        throw new RuntimeException("not implemented");
//		return new FilterExecutionJava(processor, Single.just(in));
    }

    @Override
    public FilterConfig forInput(Single<Path> futurePath) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public FilterConfig forInput(FilterConfig in) {
        return new FilterExecutionJava(processor, new DestinationFilter(in));
    }

    @Override
    public FilterConfig forInput(Destination destination) {
        return new FilterExecutionJava(processor, destination);
    }
}
