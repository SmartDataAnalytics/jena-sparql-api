package org.aksw.jena_sparql_api.playground.fuseki;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class MainPlaygroundFuseki {
	
	public static void main(String[] args) {
	    //DatasetGraph dsg = ...;
		DatasetGraph dsg = DatasetGraphFactory.createGeneral();
	    FusekiServer server = FusekiServer.create()
	        .port(1234)
	        .add("/ds", dsg)
	        .build();
	     server.start();
	}
}
