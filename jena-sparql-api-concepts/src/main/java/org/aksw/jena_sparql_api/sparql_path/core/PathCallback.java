package org.aksw.jena_sparql_api.sparql_path.core;

import org.aksw.jena_sparql_api.sparql_path.core.domain.Path;

public interface PathCallback {
	void handle(Path path);
}