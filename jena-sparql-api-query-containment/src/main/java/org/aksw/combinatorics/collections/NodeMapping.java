package org.aksw.combinatorics.collections;

import java.util.Map.Entry;

import org.aksw.commons.collections.trees.Tree;

import com.google.common.collect.Multimap;

public class NodeMapping<A, B, S> {
    protected Tree<A> aTree;
    protected Tree<B> bTree;
    protected Entry<A, B> parentMapping;
    protected Multimap<A, B> childMapping;
    protected S value;
    
    public NodeMapping(Tree<A> aTree, Tree<B> bTree, Entry<A, B> parentMapping,
            Multimap<A, B> childMapping, S value) {
        super();
        this.aTree = aTree;
        this.bTree = bTree;
        this.parentMapping = parentMapping;
        this.childMapping = childMapping;
        this.value = value;
    }
    
    public Tree<A> getTreeA() {
        return aTree;
    }

    public Tree<B> getTreeB() {
        return bTree;
    }

    public Entry<A, B> getParentMapping() {
        return parentMapping;
    }

    public Multimap<A, B> getChildMapping() {
        return childMapping;
    }

    public S getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NodeMapping [aTree=" + aTree + ", bTree=" + bTree
                + ", parentMapping=" + parentMapping + ", childMapping="
                + childMapping + ", value=" + value + "]";
    }
}
