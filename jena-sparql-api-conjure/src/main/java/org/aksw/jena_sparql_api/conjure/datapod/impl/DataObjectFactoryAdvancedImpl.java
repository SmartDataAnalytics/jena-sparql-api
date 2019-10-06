package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// DataObjectFactory with advanced handling of DataRefUrl using a repository
public class DataObjectFactoryAdvancedImpl
	extends DataObjectFactoryImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(DataObjectFactoryAdvancedImpl.class);

	protected HttpResourceRepositoryFromFileSystem repo;

	public DataObjectFactoryAdvancedImpl(
			OpVisitor<? extends RdfDataPod> opExecutor,
			HttpResourceRepositoryFromFileSystem repo) {
		super(opExecutor);

		this.repo = repo;
	}

	@Override
	public RdfDataPod visit(PlainDataRefUrl dataRef) {
		String url = dataRef.getDataRefUrl();

		RdfDataPod r;

		// HACK - http url checking should be done in the repository!
		if(url.startsWith("http://") || url.startsWith("https://")) {
			RdfHttpEntityFile entity;
			try {
				
				
				entity = HttpResourceRepositoryFromFileSystemImpl.get(repo,
						url, WebContent.contentTypeNTriples, Arrays.asList("identity"));
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			Path absPath = entity.getAbsolutePath();
			logger.debug("Resolved " + url + " to " + absPath);
			
			r = DataObjects.fromUrl(absPath.toString());
		} else {
			r = DataObjects.fromUrl(url);					
		}

		return r;
	}
};