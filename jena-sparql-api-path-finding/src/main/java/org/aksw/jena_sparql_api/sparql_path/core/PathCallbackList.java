package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;

public class PathCallbackList
	implements PathCallback
{
	private List<SimplePath> candidates = new ArrayList<SimplePath>();
	
	@Override
	public void handle(SimplePath path) {
		candidates.add(path);
	}
	
	public List<SimplePath> getCandidates() {
		return candidates;
	}
}