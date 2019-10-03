package org.aksw.jena_sparql_api.conjure.main;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataobject.impl.DataObjects;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefResource;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefResourceFromUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class MainConjurePlayground {
	public static void main(String[] args) throws Exception {
		
		// Get some download urls from the databus
		List<String> urls;
		try(RdfDataObject catalog = DataObjects.fromSparqlEndpoint("https://databus.dbpedia.org/repo/sparql", null, null)) {			
			try(RDFConnection conn = catalog.openConnection()) {
				urls = SparqlRx.execSelect(conn,
						"SELECT DISTINCT ?o { ?s <http://www.w3.org/ns/dcat#downloadURL> ?o } LIMIT 10")
					.map(qs -> qs.get("o"))
					.map(RDFNode::toString)
					.toList()
					.blockingGet();				
			}			
		}

		System.out.println("Got: " + urls);

		HttpResourceRepositoryFromFileSystem repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();
		
		OpExecutorDefault executor = new OpExecutorDefault(repo);
		
		for(String url : urls) {
			System.out.println("Processing: " + url);
	
			// Set up a dataset processing expression
			DataRefResource dataRef = DataRefResourceFromUrl.create(url);
			Op a = OpDataRefResource.from(dataRef);
			Op b = OpConstruct.create(a, "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "CONSTRUCT { ?p a rdf:Property } WHERE { { SELECT DISTINCT ?p { ?s ?p ?o } } }");
		
			System.out.println("Spec: ");
			RDFDataMgr.write(System.out, b.getModel(), RDFFormat.TURTLE);
			
			try(RdfDataObject data = b.accept(executor)) {
				try(RDFConnection conn = data.openConnection()) {
					Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					
					RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
				}
			}
		}

	}
	
	
	public static void interfaceOrderMattersForDefaultMethods() {
		DataRef test = DataRefResourceFromUrl.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		String foo = test.accept(new DataRefVisitor<String>() {

			@Override
			public String visit(DataRefFromUrl dataRef) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String visit(DataRefFromCatalog dataRef) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String visit(DataRefFromSparqlEndpoint dataRef) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String visit(DataRefExt dataRef) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		if(true) { return; }
	}
}
