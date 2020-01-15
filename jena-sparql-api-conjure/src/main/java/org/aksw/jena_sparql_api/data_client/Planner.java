package org.aksw.jena_sparql_api.data_client;

import org.apache.jena.update.UpdateRequest;

public interface Planner {
	ModelEntity plan(ModelEntity modelFlow, UpdateRequest updateRequest);
}