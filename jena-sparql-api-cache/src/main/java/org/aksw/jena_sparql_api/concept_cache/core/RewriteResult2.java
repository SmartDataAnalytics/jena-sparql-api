package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;

public class RewriteResult2 {
	public Op op;
	public Map<Node, StorageEntry> idToStorageEntry;

	public RewriteResult2(Op op, Map<Node, StorageEntry> idToStorageEntry) {
		super();
		this.op = op;
		this.idToStorageEntry = idToStorageEntry;
	}

	public Op getOp() {
		return op;
	}

	public Map<Node, StorageEntry> getIdToStorageEntry() {
		return idToStorageEntry;
	}

}
