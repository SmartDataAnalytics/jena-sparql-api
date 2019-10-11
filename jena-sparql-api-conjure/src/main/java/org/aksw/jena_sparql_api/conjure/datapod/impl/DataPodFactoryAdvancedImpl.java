package org.aksw.jena_sparql_api.conjure.datapod.impl;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;

/**
 * This class extends DataObjectFactory with advanced handling of DataRefUrl using a repository
 * 
 * @author raven
 *
 */
public class DataPodFactoryAdvancedImpl
	extends DataPodFactoryImpl {
	
	//private static final Logger logger = LoggerFactory.getLogger(DataPodFactoryAdvancedImpl.class);

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