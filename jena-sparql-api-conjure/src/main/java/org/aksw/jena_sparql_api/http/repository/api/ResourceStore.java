package org.aksw.jena_sparql_api.http.repository.api;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.apache.jena.rdf.model.Resource;

public interface ResourceStore {
	
	public static String readHash(RdfHttpEntityFile entity, String hashName) {
		RdfEntityInfo info = entity.getCombinedInfo().as(RdfEntityInfo.class);
		Checksum hashInfo = info.getHash(hashName);
		String result = Optional.ofNullable(hashInfo)
				.map(Checksum::getChecksum)
				.orElse(null);

		return result;
	}
	
	/**
	 * Bridge between paths and entities
	 * 
	 * @param absEntityPath Absolute path to an entity.
	 * @return
	 */
	RdfHttpEntityFile getEntityForPath(Path absEntityPath);

	
	RdfHttpResourceFile getResource(String uri);	
	RdfHttpEntityFile allocateEntity(String uri, Resource description);

	Path getAbsolutePath();

	/*
	 * File system based methods
	 */
	
	Collection<RdfHttpEntityFile> listEntities(Path basePath);

	/**
	 * Test whether the path lies within the store - does not check for existence
	 * @param path
	 * @return
	 */
	boolean contains(Path path);

	/**
	 * Return the metadata associated with a given path
	 * 
	 * @param path
	 * @param layer A label to retrieve the metadata from a single source
	 * @return
	 */
	Resource getInfo(Path path, String layer);

	void updateInfo(Path path, Consumer<? super Resource> info);
	
	default Resource getInfo(Path path) {
		Resource result = getInfo(path, null);
		return result;
	}

	RdfHttpEntityFile allocateEntity(Path relPath, Resource description);
}
