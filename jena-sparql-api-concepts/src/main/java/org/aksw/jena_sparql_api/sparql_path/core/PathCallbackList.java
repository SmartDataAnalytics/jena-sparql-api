package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.sparql_path.core.domain.Path;

public class PathCallbackList
	implements PathCallback
{
	private List<Path> candidates = new ArrayList<Path>();
	
	@Override
	public void handle(Path path) {
		candidates.add(path);
	}
	
	public List<Path> getCandidates() {
		return candidates;
	}
}