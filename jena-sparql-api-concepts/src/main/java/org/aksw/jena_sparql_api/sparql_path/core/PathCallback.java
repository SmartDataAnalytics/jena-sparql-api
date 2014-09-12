package org.aksw.jena_sparql_api.sparql_path.core;

import org.aksw.jena_sparql_api.concepts.Path;

public interface PathCallback {
	void handle(Path path);
}