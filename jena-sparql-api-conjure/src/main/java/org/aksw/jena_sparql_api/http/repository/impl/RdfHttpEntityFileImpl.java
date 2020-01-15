package org.aksw.jena_sparql_api.http.repository.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.jena.rdf.model.Resource;

public class RdfHttpEntityFileImpl
	implements RdfHttpEntityFile
{
	protected RdfHttpResourceFile resource;
	
	// relative file or folder within the resource that denotes the entity
	protected Path relPath;

	public RdfHttpEntityFileImpl(RdfHttpResourceFile resource, Path path) {
		super();
		this.resource = resource;
		this.relPath = path;
	}

	@Override
	public RdfHttpResourceFile getResource() {
		return resource;
	}

	@Override
	public Path getRelativePath() {
		return relPath;
	}

	@Override
	public Resource getCombinedInfo() {
		Path absPath = getAbsolutePath();
		Resource result = resource.getResourceStore().getInfo(absPath);
		return result;
	}

	@Override
	public void updateInfo(Consumer<? super Resource> consumer) {
		ResourceStore store = getResource().getResourceStore();
		store.updateInfo(getAbsolutePath(), consumer);
	}

	@Override
	public String toString() {
		return relPath + " via " + getAbsolutePath();
	}

	@Override
	public InputStream open() throws IOException {
		Path absPath = getAbsolutePath();
		InputStream result = Files.newInputStream(absPath, StandardOpenOption.READ);
		return result;
	}	
}
