package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import io.reactivex.Single;

public class HotFileFromJava
	implements HotFile
{
	protected ConcurrentFileEndpoint endpoint;
	//protected Single<?> processSingle;
	
	public HotFileFromJava(ConcurrentFileEndpoint endpoint) {
		super();
		this.endpoint = endpoint;		
	}
	
	@Override
	public CompletableFuture<Path> future() {
		return endpoint.getIsDone();
	}

	@Override
	public void abort() {
		//processSingle.
	}

	@Override
	public InputStream newInputStream() throws IOException {
		InputStream result = Channels.newInputStream(endpoint.newReadChannel());
		return result;
	}

}
