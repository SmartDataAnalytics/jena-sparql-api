package org.aksw.jena_sparql_api.http.repository.impl;

import java.nio.file.Path;
import java.util.Collection;

import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.jena.rdf.model.Resource;

public class RdfHttpResourceFileImpl
	implements RdfHttpResourceFile
{
	protected ResourceStore manager;
	protected Path path;
	
	public RdfHttpResourceFileImpl(ResourceStore manager, Path path) {
		super();
		this.manager = manager;
		this.path = path;
	}

//	@Override
//	public ResourceManager getManager() {
//		return manager;
//	}

	@Override
	public Collection<RdfHttpEntityFile> getEntities() {
		return manager.listEntities(path);
	}

	@Override
	public Path getRelativePath() {
		return path;
	}

	@Override
	public ResourceStore getResourceStore() {
		return manager;
	}

	@Override
	public RdfHttpEntityFile allocate(Resource description) {
		RdfHttpEntityFile result = getResourceStore().allocateEntity(path, description);
		return result;
	}

	@Override
	public RdfHttpResourceFile resolve(String path) {
		Path targetPath = getRelativePath().resolve(path);
		RdfHttpResourceFile result = new RdfHttpResourceFileImpl(manager, targetPath);
		return result;
		//getResourceStore().getResource(uri)
	}
	
}
