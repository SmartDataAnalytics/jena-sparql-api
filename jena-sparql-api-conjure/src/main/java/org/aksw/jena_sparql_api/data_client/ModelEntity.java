package org.aksw.jena_sparql_api.data_client;

public interface ModelEntity {
	boolean isMemory();
	ModelEntityMemory asMemory();
	
	boolean isFile();
	ModelEntityFile asFile();
	
	boolean isUrl();
}
