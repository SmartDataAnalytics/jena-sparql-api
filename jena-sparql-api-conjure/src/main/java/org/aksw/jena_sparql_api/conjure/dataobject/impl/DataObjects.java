package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataObjects {
	private static final Logger logger = LoggerFactory.getLogger(DataObjects.class);
	
	public static RdfDataObject fromDataRef(DataRef dataRef, HttpResourceRepositoryFromFileSystem repo) {
		
		DataRefVisitor<RdfDataObject> defaultFactory = new DataObjectFactoryImpl() {
			@Override
			public RdfDataObject visit(DataRefFromUrl dataRef) {
				String url = dataRef.getDataRefUrl();
				RdfHttpEntityFile entity;
				try {
					entity = HttpResourceRepositoryFromFileSystemImpl.get(repo,
							url, WebContent.contentTypeNTriples, Arrays.asList("identity"));
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				Path absPath = entity.getAbsolutePath();
				logger.debug("Resolved " + url + " to " + absPath);
				
				RdfDataObject r = DataObjects.fromUrl(absPath.toString());
				return r;
			}
		};
		
//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
		RdfDataObject result = dataRef.accept(defaultFactory);
		return result;
	}

	
	public static RdfDataObject fromDataRef(DataRef dataRef) {
		DataRefVisitor<RdfDataObject> defaultFactory = new DataObjectFactoryImpl();
		
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
		Model model = RDFDataMgr.loadModel(url);		
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