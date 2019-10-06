package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.URIUtils;
import org.aksw.jena_sparql_api.utils.hdt.JenaPluginHdt;
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
		
		PlainDataRefVisitor<RdfDataPod> factory = new DataObjectFactoryAdvancedImpl(opExecutor, repo);
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataPod result = dataRef.accept(factory);
		return result;
	}

	
	public static RdfDataPod fromDataRef(PlainDataRef dataRef, OpVisitor<? extends RdfDataPod> opExecutor) {
		PlainDataRefVisitor<RdfDataPod> defaultFactory = new DataObjectFactoryImpl(opExecutor);
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataPod result = dataRef.accept(defaultFactory);
		return result;
	}
	
	public static RdfDataPod fromModel(Model model) {
		Dataset dataset = DatasetFactory.wrap(model);
		
		return new RdfDataObjectBase() {
			@Override
			protected RDFConnection newConnection() {
				RDFConnection result = RDFConnectionFactory.connect(dataset);
				return result;
			}
		};
	}

	public static RdfDataPod fromUrl(PlainDataRefUrl dataRef) {
		String url = dataRef.getDataRefUrl();
		RdfDataPod result = fromUrl(url);
		return result;
	}

	
	public static RdfDataPod fromUrl(String url) {
		logger.info("Loading: " + url);
		
		Lang lang = RDFLanguages.resourceNameToLang(url);
		Model model;
		if(JenaPluginHdt.LANG_HDT.equals(lang)) {
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

		
		RdfDataPod result = DataObjects.fromModel(model);
		return result;
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
		
		Supplier<RDFConnection> supplier = () -> RDFConnectionRemote.create()
				.destination(serviceUrl)
				.acceptHeaderSelectQuery(WebContent.contentTypeResultsXML) // JSON breaks on virtuoso with empty result sets
				.build();


		RdfDataPod result = fromConnectionSupplier(supplier);
		return result;
	}
	

	public static RdfDataPod fromConnectionSupplier(Supplier<? extends RDFConnection> supplier) {
		return new RdfDataObjectBase() {
			@Override
			protected RDFConnection newConnection() {
				RDFConnection result = supplier.get();
				return result;
			}
		};
	}
}
