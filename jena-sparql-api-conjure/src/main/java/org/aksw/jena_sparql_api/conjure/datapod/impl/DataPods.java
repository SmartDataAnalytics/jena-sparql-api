package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.aksw.commons.io.util.UriUtils;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.io.hdt.JenaPluginHdt;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.util.ResourceUtils;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic DataPod factory methods
 * 
 * @author raven
 *
 */
public class DataPods {
	private static final Logger logger = LoggerFactory.getLogger(DataPods.class);
	
	public static RdfDataPod fromDataRef(DataRef dataRef) {
		//OpExecutorDefault catalogExecutor = new OpExecutorDefault(null, null, new LinkedHashMap<>(), RDFFormat.TURTLE_PRETTY);

		Resource rawCopy = dataRef.inModel(ResourceUtils.reachableClosure(dataRef));
		DataRef copy = JenaPluginUtils.polymorphicCast(rawCopy, DataRef.class);
		Op basicWorkflow = OpDataRefResource.from(copy.getModel(), copy);
		RdfDataPod result = ExecutionUtils.executeJob(basicWorkflow);
		//RdfDataPod result = basicWorkflow.accept(catalogExecutor);
		return result;
	}
	
	public static RdfDataPod empty() {
		RdfDataPod result = fromModel(ModelFactory.createDefaultModel());
		return result;
	}

	public static RdfDataPod fromData(Object data) {
		if(data != null) {
			throw new RuntimeException("not implemented yet");
		}
		RdfDataPod result = fromModel(ModelFactory.createDefaultModel());
		return result;
	}

	public static RdfDataPod fromDataRef(PlainDataRef dataRef, HttpResourceRepositoryFromFileSystem repo, OpVisitor<? extends RdfDataPod> opExecutor) {
		
		PlainDataRefVisitor<RdfDataPod> factory = new DataPodFactoryAdvancedImpl(opExecutor, repo);
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataPod result = dataRef.accept(factory);
		return result;
	}

	
	public static RdfDataPod fromDataRef(PlainDataRef dataRef, OpVisitor<? extends RdfDataPod> opExecutor) {
		PlainDataRefVisitor<RdfDataPod> defaultFactory = new DataPodFactoryImpl(opExecutor);
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataPod result = dataRef.accept(defaultFactory);
		return result;
	}
	
	public static RdfDataPod fromDataset(Dataset dataset) {
		return new RdfDataPodBase() {
			@Override
			protected RDFConnection newConnection() {
				RDFConnection result = RDFConnectionFactory.connect(dataset);
				return result;
			}
			
			/**
			 * Only returns the default model
			 */
			@Override
			public Model getModel() {
				Model r = dataset.getDefaultModel();
				return r;
			}
		};
	}

	
	public static RdfDataPod fromModel(Model model) {
		Dataset dataset = DatasetFactory.wrap(model);

		RdfDataPod result = fromDataset(dataset);
		return result;
		
//		return new RdfDataPodBase() {
//			@Override
//			protected RDFConnection newConnection() {
//				RDFConnection result = RDFConnectionFactory.connect(dataset);
//				return result;
//			}
//			
//			@Override
//			public Model getModel() {
//				return model;
//			}
//		};
	}

	public static RdfDataPod fromUrl(PlainDataRefUrl dataRef) {
		String url = dataRef.getDataRefUrl();
		RdfDataPod result = fromUrl(url);
		return result;
	}

	
	public static RdfDataPod fromUrl(String url) {
		logger.info("Loading: " + url);
		
		RdfDataPod result;
		Lang lang = RDFLanguages.resourceNameToLang(url);
		if(JenaPluginHdt.LANG_HDT.equals(lang)) {
			logger.info("HDT file detected - loading using HDT graph " + url);
			// Only allow local file URLs
			Path path = Paths.get(UriUtils.newURI(url));
			String pathStr = path.toString();
			
			HDT hdt;
			try {
				//hdt = HDTManager.loadHDT(pathStr);
				// Map seems to be significantly faster than load for cases where we have
				// to scan all triples anyway
				// TODO The load method should take an example query load in order to decide
				// the best way of loading
				hdt = HDTManager.mapHDT(pathStr);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			logger.info("Loading of hdt complete " + pathStr);

			Ref<HDT> hdtRef = RefImpl.create(hdt,
					() -> {
						logger.debug("Closed HDT file: " + pathStr);
						hdt.close();
					}, "HDT Data Pod from " + pathStr);
			result = new RdfDataPodHdtImpl(hdtRef, false);
		} else {
			Model model;
			model = RDFDataMgr.loadModel(url);
			result = DataPods.fromModel(model);
		}

		
		return result;
	}

	
	public static RdfDataPod create(String url, HttpResourceRepositoryFromFileSystem repo) {
		RdfDataPod r;

		// HACK - http url checking should be done in the repository!
		if(url.startsWith("http://") || url.startsWith("https://")) {
			RdfHttpEntityFile entity;
			try {
				HttpUriRequest baseRequest =
						RequestBuilder.get(url)
						.setHeader(HttpHeaders.ACCEPT, "application/x-hdt")
						.setHeader(HttpHeaders.ACCEPT_ENCODING, "identity,bzip2,gzip")
						.build();

				HttpRequest effectiveRequest = HttpResourceRepositoryFromFileSystemImpl.expandHttpRequest(baseRequest);
				logger.info("Expanded HTTP Request: " + effectiveRequest);
				
				entity = repo.get(effectiveRequest, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);

				logger.info("Response entity is: " + entity);
				
//				repo.get(, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);
				
//				entity = HttpResourceRepositoryFromFileSystemImpl.get(repo,
//						url, WebContent.contentTypeNTriples, Arrays.asList("identity"));
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			Path absPath = entity.getAbsolutePath();
			logger.info("Resolved " + url + " to " + absPath);
			
			r = DataPods.fromUrl(absPath.toUri().toString());
		} else {
			r = DataPods.fromUrl(url);					
		}

		return r;
	}
	public static RdfDataPod fromSparqlEndpoint(PlainDataRefSparqlEndpoint dataRef) {
		String serviceUrl = dataRef.getServiceUrl();
		//DatasetDescription dd = dataRef.getDatsetDescription();
		
		List<String> defaultGraphs = dataRef.getDefaultGraphs(); 
		List<String> namedGraphs = dataRef.getNamedGraphs();
		
		RdfDataPod result = fromSparqlEndpoint(serviceUrl, defaultGraphs, namedGraphs);
		return result;
	}

	public static RdfDataPod fromSparqlEndpoint(String serviceUrl, List<String> defaultGraphs, List<String> namedGraphs) {
		
		Objects.requireNonNull(serviceUrl, "Service URL must not be null");
		
		Supplier<RDFConnection> supplier = () -> RDFConnectionRemote.create()
				.destination(serviceUrl)
				.acceptHeaderSelectQuery(WebContent.contentTypeResultsXML) // JSON breaks on virtuoso with empty result sets
				.build();


		RdfDataPod result = fromConnectionSupplier(supplier);
		return result;
	}
	

	public static RdfDataPod fromConnectionSupplier(Supplier<? extends RDFConnection> supplier) {
		return new RdfDataPodBase() {
			@Override
			protected RDFConnection newConnection() {
				RDFConnection result = supplier.get();
				return result;
			}
		};
	}
}
