package org.aksw.jena_sparql_api.io.filter.sys;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.aksw.commons.io.endpoint.FileCreation;

/**
 * Wrap an existing file as a FileCreation that has completed
 * 
 * @author raven
 *
 */
public class FileCreationWrapper
	implements FileCreation
{
	protected Path path;
	protected CompletableFuture<Path> future;

	
	public FileCreationWrapper(Path path) {
		super();
		this.path = path;
		this.future = new CompletableFuture<Path>();
		this.future.complete(path);
	}
	
	@Override
	public CompletableFuture<Path> future() {
		return future;
	}

	@Override
	public void abort() {
		/* The file already exists, so there is nothing to abort */
	}
}
