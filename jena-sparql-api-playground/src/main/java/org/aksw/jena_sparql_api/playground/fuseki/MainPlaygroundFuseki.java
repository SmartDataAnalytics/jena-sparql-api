package org.aksw.jena_sparql_api.playground.fuseki;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class MainPlaygroundFuseki {
	
	public static void main(String[] args) throws Exception {
//		<dependency>
//		<groupId>org.apache.jena</groupId>
//		<artifactId>jena-fuseki-main</artifactId>
//		<version>${jena.version}</version>
//	</dependency>
//
//// 	<repositories>
//		<repository>
//		<id>maven.aksw.snapshots</id>
//		<name>AKSW Snapshot Repository</name>
//		<url>http://maven.aksw.org/archiva/repository/snapshots</url>
//		<releases>
//			<enabled>false</enabled>
//		</releases>
//		<snapshots>
//			<enabled>true</enabled>
//		</snapshots>
//	</repository>
//</repositories>
//	<dependency>
//		<groupId>org.aksw.jena-sparql-api</groupId>
//		<artifactId>jena-sparql-api-io-hdt</artifactId>
//	</dependency>

		
		//Model m = RDFDataMgr.loadModel("/home/raven/public_html/009e80050fa7f4279596956477157ec2.hdt");
		
//		Iterator<Triple> it = RDFDataMgr.createIteratorTriples(new FileInputStream("/home/raven/public_html/test.hdt"), JenaPluginHdt.LANG_HDT, "http://foo/"); 
//		while(it.hasNext()) {
//			System.out.println(it.next());
//		}
		
		HDT hdt = HDTManager.loadHDT("/home/raven/public_html/test.hdt");
		Graph g = new HDTGraph(hdt);
		DatasetGraph dsg = DatasetFactory.wrap(ModelFactory.createModelForGraph(g)).asDatasetGraph();
		
		try(QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create("SELECT * { ?s ?p ?o } LIMIT 10 OFFSET 100"), dsg)) {
			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
		}
		
		
		//RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES);
		
//	    //DatasetGraph dsg = ...;
		//DatasetGraph dsg = DatasetGraphFactory.createGeneral();
//	    FusekiServer server = FusekiServer.create()
//	        .port(12234)
//	        .add("/ds", dsg)
//	        .build();
//	     server.start();
	}
}
