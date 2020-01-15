package org.aksw.jena_sparql_api.data_client;

import org.apache.jena.rdf.model.Model;

public interface ModelFlowDriver {
	ModelFlow connect(Model model);
}
