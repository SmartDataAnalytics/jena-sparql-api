package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.apache.jena.graph.Node;

public class ProjectionEntry {
	//protected Node patternId;
	protected VarInfo varInfo;
	protected Node storageId;

	public ProjectionEntry(VarInfo varInfo, Node storageId) {
		super();
		this.varInfo = varInfo;
		this.storageId = storageId;
	}

	public VarInfo getVarInfo() {
		return varInfo;
	}

	public Node getStorageId() {
		return storageId;
	}

	@Override
	public String toString() {
		return "ProjectionEntry [varInfo=" + varInfo + ", storageId=" + storageId + "]";
	}
}
