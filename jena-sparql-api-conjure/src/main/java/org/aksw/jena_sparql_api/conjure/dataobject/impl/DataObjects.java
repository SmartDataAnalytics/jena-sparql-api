package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.conjure.dataobject.api.DataObject;
import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetDescription;

public class DataObjects {
	public static DataObject fromDataRef(DataRef dataRef) {
		DataRefVisitor<DataObject> defaultFactory = new DataObjectFactoryImpl();
		
		DataObject result = dataRef.accept(defaultFactory);
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
		DatasetDescription dd = dataRef.getDatsetDescription();
		
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
