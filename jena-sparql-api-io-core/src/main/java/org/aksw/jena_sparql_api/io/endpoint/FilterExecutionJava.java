package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.io.ByteStreams;

import io.reactivex.Single;

public class FilterExecutionJava
	implements FilterConfig
{
	protected Function<InputStream, InputStream> processor;
	protected InputStreamSupplier inputStreamSupplier;

	public FilterExecutionJava(Function<InputStream, InputStream> processor, InputStreamSupplier inputStreamSupplier) {
		this.processor = processor;
		this.inputStreamSupplier = inputStreamSupplier;
	}
	
	@Override
	public FileWritingProcess execToFile(Path path) throws IOException {
		ConcurrentFileEndpoint out = ConcurrentFileEndpoint.create(path, StandardOpenOption.CREATE);
		try(InputStream in = inputStreamSupplier.execStream()) {
			ByteStreams.copy(in, Channels.newOutputStream(out));
		}
		
		return null;
	}

	/**
	 * Ideally, premature closing of the input stream should
	 * propagate upstream and terminate any transformation processes upstream
	 * 
	 */
	@Override
	public Single<InputStreamSupplier> execStream() {
		return Single.just(inputStreamSupplier).map(inSupp -> {
			return () -> {
				InputStream in = inputStreamSupplier.execStream();
				InputStream r = processor.apply(in);
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
	@Override
	public InputStream execStream(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback) throws IOException {
		InputStream result = execStream();
		return result;
	}

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Destination outputToStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean requiresFileOutput() {
		return false;
	}


}
