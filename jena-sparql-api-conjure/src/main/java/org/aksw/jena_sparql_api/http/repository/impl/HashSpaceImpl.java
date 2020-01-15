package org.aksw.jena_sparql_api.http.repository.impl;

import java.nio.file.Path;
import java.util.List;

import org.aksw.jena_sparql_api.http.repository.api.HashSpace;

import com.google.common.base.Splitter;

public class HashSpaceImpl
	implements HashSpace
{
	protected Path basePath;
	// partition hash by subfolders of length
	protected int hashSplitLength;

	public HashSpaceImpl(Path basePath) {
		this(basePath, 4);
	}

	public HashSpaceImpl(Path basePath, int hashSplitLength) {
		super();
		this.basePath = basePath;
		this.hashSplitLength = hashSplitLength;
	}

	/**
	 * Copies or moves a file into the hash space
	 * and returns the path
	 * 
	 * 
	 * @param hash
	 * @param file
	 * @param move if true, move the given file into the hash space
	 * @return
	 */
	public Path get(String hash) {

		List<String> parts = Splitter
			.fixedLength(hashSplitLength)
			.splitToList(hash);

		Path result = basePath;
		for(String part : parts) {
			result = result.resolve(part);
		}
		
		return result;
	}
}