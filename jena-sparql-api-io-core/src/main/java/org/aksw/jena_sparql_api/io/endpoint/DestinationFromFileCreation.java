package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.reactivex.Single;

/**
 * Destination of a file that does not yet exist
 * 
 * @author raven
 *
 */
public class DestinationFromFileCreation
	implements Destination
{
	protected Single<FileCreation> fileCreation;
	protected Path fileBeingCreated;
	
	
	public DestinationFromFileCreation(Path fileBeingCreated, Single<FileCreation> fileCreation) {
		super();
		this.fileBeingCreated = fileBeingCreated;
		this.fileCreation = fileCreation;
	}
	
	public Single<FileCreation> getFileCreation() {
		return fileCreation;
	}
	
	/**
	 * The file being created once there is a subscription to the single
	 * 
	 * @return
	 */
	public Path getFileBeingCreated() {
		return fileBeingCreated;
	}
	
	@Override
	public FilterConfig transferTo(FilterEngine engine) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Single<InputStreamSupplier> prepareStream() {
		return fileCreation.flatMap(fc -> {
			Single<InputStreamSupplier> r;
			if(fc instanceof HotFile) {
				HotFile hotFile = (HotFile)fc;
				r = Single.just((InputStreamSupplier)hotFile::newInputStream);//((HotFile)fc)::newInputStream;
			} else {
				r = Single.fromFuture(fc.future()).map(path -> {
					return (InputStreamSupplier) Files.newInputStream(path, StandardOpenOption.READ);
				});
			}
			
			return r;
		});
		
	}
}
