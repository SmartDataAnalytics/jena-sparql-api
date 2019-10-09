package org.aksw.jena_sparql_api.io.endpoint;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

import io.reactivex.Single;

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
		return new FilterExecutionJava(processor,
				() -> Files.newInputStream(in, StandardOpenOption.READ));
	}

	@Override
	public FilterConfig forInput(InputStreamSupplier in) {
		return new FilterExecutionJava(processor, in);
	}

	@Override
	public FilterConfig forInput(Single<Path> futurePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterConfig forInput(FilterConfig in) {
		return new FilterExecutionJava(processor,
				() -> in.execStream());
	}
}
