package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.data_client.ModelEntity;

public interface OpExecutor {
	ModelEntity load(String datasetId);
}
