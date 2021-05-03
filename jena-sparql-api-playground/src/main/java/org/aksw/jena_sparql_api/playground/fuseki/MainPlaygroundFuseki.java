package org.aksw.jena_sparql_api.playground.fuseki;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class MainPlaygroundFuseki {
	
	public static void main(String[] args) throws Exception {
		//Model m = RDFDataMgr.loadModel("/home/raven/public_html/009e80050fa7f4279596956477157ec2.hdt");
		
//		Iterator<Triple> it = RDFDataMgr.createIteratorTriples(new FileInputStream("/home/raven/public_html/test.hdt"), JenaPluginHdt.LANG_HDT, "http://foo/"); 
//		while(it.hasNext()) {
//			System.out.println(it.next());
//		}
		
		HDT hdt = HDTManager.loadHDT("/home/raven/public_html/test.hdt");
		Graph g = new HDTGraph(hdt);
		DatasetGraph dsg = DatasetFactory.wrap(ModelFactory.createModelForGraph(g)).asDatasetGraph();
		
//		try(QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create("SELECT * { ?s ?p ?o } LIMIT 10 OFFSET 100"), dsg)) {
//			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//		}
		
		
		
		//RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES);
		
//	    //DatasetGraph dsg = ...;
		//DatasetGraph dsg = DatasetGraphFactory.createGeneral();
//	    FusekiServer server = FusekiServer.create()
//	        .port(1234)
//	        .add("/ds", dsg)
//	        .build();
//	     server.start();
	}
}
