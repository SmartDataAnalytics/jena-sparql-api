package org.aksw.jena_sparql_api.io.filter.engine;

import java.nio.file.Path;
import java.util.List;

import org.aksw.jena_sparql_api.io.endpoint.InputStreamSupplier;

public class FilterChainExecutor {
	protected List<FilterExecutor> executors;
	
	FilterExecutor forInput(Path in) {
		return null;
	}
	
	FilterExecutor forInput(InputStreamSupplier in) {
		return null;
	}
	
	//addFilter();
	
	
	
}
