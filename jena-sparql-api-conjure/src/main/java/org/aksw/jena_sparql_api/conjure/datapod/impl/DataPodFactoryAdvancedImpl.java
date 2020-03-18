package org.aksw.jena_sparql_api.conjure.datapod.impl;

import org.aksw.dcat.ap.utils.DcatUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends DataObjectFactory with advanced handling of DataRefUrl using a repository
 * 
 * @author raven
 *
 */
public class DataPodFactoryAdvancedImpl
	extends DataPodFactoryImpl {
	
	private static Logger logger = LoggerFactory.getLogger(DataPodFactoryAdvancedImpl.class);

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

		TaskContext context = ((OpExecutorDefault)opExecutor).getTaskContext();
		Model m = context == null ? null : context.getCtxModels().get(url);
		RdfDataPod result;
		if(m != null) {
			logger.info("Accessed input model for url " + url);
			result = DataPods.fromModel(m);
		} else {
			result = DataPods.create(url, repo);
		}
		

//		RdfDataPod result = DataPods.create(url, repo);
		return result;
	}
	

	@Override
	public RdfDataPod visit(PlainDataRefDcat dataRef) {
	
		//RDFDataMgr.write(System.out, dataRef.getDcatResource().getModel(), RDFFormat.TURTLE_PRETTY);
		
		Resource dcatRecord = dataRef.getDcatRecord();
		
		String url = DcatUtils.getFirstDownloadUrl(dcatRecord);
		if(url == null) {
			throw new RuntimeException("Could not obtain a datasource from " + dcatRecord);
		}
		
		RdfDataPod result = DataPods.create(url, repo);
		return result;
	}
	
	
	@Override
	public RdfDataPod visit(PlainDataRefGit dataRef) {
		return super.visit(dataRef);
	}
};