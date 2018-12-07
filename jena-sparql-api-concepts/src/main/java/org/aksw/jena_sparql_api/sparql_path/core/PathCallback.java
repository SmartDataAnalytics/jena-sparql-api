package org.aksw.jena_sparql_api.sparql_path.core;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;

public interface PathCallback {
	void handle(SimplePath path);
}