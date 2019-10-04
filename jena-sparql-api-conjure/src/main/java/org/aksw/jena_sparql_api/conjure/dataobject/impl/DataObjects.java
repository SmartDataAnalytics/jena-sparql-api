package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.http.repository.impl.URIUtils;
import org.aksw.jena_sparql_api.utils.turtle.JenaPluginHdt;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataObjects {
	private static final Logger logger = LoggerFactory.getLogger(DataObjects.class);
	
	public static RdfDataObject empty() {
		RdfDataObject result = fromModel(ModelFactory.createDefaultModel());
		return result;
	}

	public static RdfDataObject fromData(Object data) {
		if(data != null) {
			throw new RuntimeException("not implemented yet");
		}
		RdfDataObject result = fromModel(ModelFactory.createDefaultModel());
		return result;
	}

	public static RdfDataObject fromDataRef(DataRef dataRef, HttpResourceRepositoryFromFileSystem repo, OpVisitor<? extends RdfDataObject> opExecutor) {
		
		DataRefVisitor<RdfDataObject> defaultFactory = new DataObjectFactoryImpl(opExecutor) {
			@Override
			public RdfDataObject visit(DataRefFromUrl dataRef) {
				String url = dataRef.getDataRefUrl();

				RdfDataObject r;

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
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataObject result = dataRef.accept(defaultFactory);
		return result;
	}

	
	public static RdfDataObject fromDataRef(DataRef dataRef, OpVisitor<? extends RdfDataObject> opExecutor) {
		DataRefVisitor<RdfDataObject> defaultFactory = new DataObjectFactoryImpl(opExecutor);
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataObject result = dataRef.accept(defaultFactory);
		return result;
	}
	
	public static RdfDataObject fromModel(Model model) {
		Dataset dataset = DatasetFactory.wrap(model);
		
		return new RdfDataObjectBase() {
			@Override
			protected RDFConnection newConnection() {
				RDFConnection result = RDFConnectionFactory.connect(dataset);
				return result;
			}
		};
	}

	public static RdfDataObject fromUrl(DataRefFromUrl dataRef) {
		String url = dataRef.getDataRefUrl();
		RdfDataObject result = fromUrl(url);
		return result;
	}

	
	public static RdfDataObject fromUrl(String url) {
		logger.info("Loading: " + url);
		
		Lang lang = RDFLanguages.resourceNameToLang(url);
		Model model;
		if(JenaPluginHdt.HDT.equals(lang)) {
			// Only allow local file URLs
			Path path = Paths.get(URIUtils.newURI(url));
			String pathStr = path.toString();
			
			HDT hdt;
			try {
				hdt = HDTManager.loadHDT(pathStr);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// Create Jena Model on top of HDT.
			HDTGraph graph = new HDTGraph(hdt);
			model = ModelFactory.createModelForGraph(graph);
		} else {
			model = RDFDataMgr.loadModel(url);
		}

		
		RdfDataObject result = DataObjects.fromModel(model);
		return result;
	}

	public static RdfDataObject fromSparqlEndpoint(DataRefFromSparqlEndpoint dataRef) {
		String serviceUrl = dataRef.getServiceUrl();
		//DatasetDescription dd = dataRef.getDatsetDescription();
		
		List<String> defaultGraphs = dataRef.getDefaultGraphs(); 
		List<String> namedGraphs = dataRef.getNamedGraphs();
		
		RdfDataObject result = fromSparqlEndpoint(serviceUrl, defaultGraphs, namedGraphs);
		return result;
	}

	public static RdfDataObject fromSparqlEndpoint(String serviceUrl, List<String> defaultGraphs, List<String> namedGraphs) {
		
		Supplier<RDFConnection> supplier = () -> RDFConnectionRemote.create()
				.destination(serviceUrl)
				.acceptHeaderSelectQuery(WebContent.contentTypeResultsXML) // JSON breaks on virtuoso with empty result sets
				.build();


		RdfDataObject result = fromConnectionSupplier(supplier);
		return result;
	}
	

	public static RdfDataObject fromConnectionSupplier(Supplier<? extends RDFConnection> supplier) {
		return new RdfDataObjectBase() {
			@Override
			protected RDFConnection newConnection() {
				RDFConnection result = supplier.get();
				return result;
			}
		};
	}
}
