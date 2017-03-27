package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;

public class RewriteResult2 {
    protected Op op;
    protected Map<Node, StorageEntry> idToStorageEntry;
    protected int rewriteLevel; // 0 = no rewrite, 1 = partial rewrite, 2 = full rewrite

    public RewriteResult2(Op op, Map<Node, StorageEntry> idToStorageEntry, int rewriteLevel) {
        super();
        this.op = op;
        this.idToStorageEntry = idToStorageEntry;
        this.rewriteLevel = rewriteLevel;
    }

    public Op getOp() {
        return op;
    }

    public Map<Node, StorageEntry> getIdToStorageEntry() {
        return idToStorageEntry;
    }

    public int getRewriteLevel() {
        return rewriteLevel;
    }

}
