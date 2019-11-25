package org.aksw.jena_sparql_api.http.repository.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.apache.jena.rdf.model.Resource;

public interface ResourceStore {

	// TODO Maybe add here or on a sub-interface
	// facilities for post-entity-creation; i.e. put trigger that can compute checksums
	// on entities

	public static String readHash(RdfHttpEntityFile entity, String hashName) {
		RdfEntityInfo info = entity.getCombinedInfo().as(RdfEntityInfo.class);
		Checksum hashInfo = info.getHash(hashName);
		String result = Optional.ofNullable(hashInfo)
				.map(Checksum::getChecksum)
				.orElse(null);

		return result;
	}
	
	/**
	 * Archive the file in the repository and then delete the source
	 * 
	 * TODO This method can be considered mostly as simple convenience function:
	 * We may want some more sophisticated API
	 * that places a file or stream into the repository once it becomes ready and triggers
	 * an action when the archiving has completed - something along the lines of:
	 * 
	 * flowableSourceOfByteSourceOrFile
	 * 	.flatMap(repo.archivingThatProducesASingleWithInfo(byteSourceOrFile))
	 * .subscribe(info -> info.inputFile().delete())
	 * 
	 * @param uri
	 * @param metadata
	 * @param file
	 * @return
	 */
	RdfHttpEntityFile putWithMove(String uri, RdfEntityInfo metadata, Path file) throws IOException;
	
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
