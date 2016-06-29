package org.aksw.jena_sparql_api.concept_cache.dirty;

public interface TreeNode<T> {
    Tree<T> getTree();
    TreeNode<T> getNode();
}
