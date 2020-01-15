package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.reactivex.Single;

/**
 * Destination from an existing file
 * 
 * @author raven
 *
 */
public class DestinationFromFile
	implements Destination
{
	protected Path path;

	public Path getPath() {
		return path;
	}
	
	public DestinationFromFile(Path path) {
		super();
		this.path = path;
	}
	
	@Override
	public FilterConfig transferTo(FilterEngine engine) {
		FilterConfig result = engine.forInput(path);
		return result;
	}

	@Override
	public Single<InputStreamSupplier> prepareStream() {
		return Single.just(() -> Files.newInputStream(path, StandardOpenOption.READ));
	}
}
