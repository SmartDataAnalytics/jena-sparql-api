package org.aksw.jena_sparql_api.conjure.datapod.impl;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// DataObjectFactory with advanced handling of DataRefUrl using a repository
public class DataPodFactoryAdvancedImpl
	extends DataPodFactoryImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(DataPodFactoryAdvancedImpl.class);

	protected HttpResourceRepositoryFromFileSystem repo;

	public DataPodFactoryAdvancedImpl(
			OpVisitor<? extends RdfDataPod> opExecutor,
			HttpResourceRepositoryFromFileSystem repo) {
		super(opExecutor);

		this.repo = repo;
	}

	
	@Override
	public RdfDataPod visit(PlainDataRefUrl dataRef) {
		String url = dataRef.getDataRefUrl();

		RdfDataPod result = DataPods.create(url, repo);
		return result;
	}
};