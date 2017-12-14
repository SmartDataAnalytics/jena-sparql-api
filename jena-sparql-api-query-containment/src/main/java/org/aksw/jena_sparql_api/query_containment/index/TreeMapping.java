package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.collections.trees.Tree;

import com.google.common.collect.Table;

public class TreeMapping<A, B, S, M> {
    protected Tree<A> aTree;
    protected Tree<B> bTree;
    protected S overallMatching;
    protected Table<A, B, M> nodeMappings;

    public TreeMapping(Tree<A> aTree, Tree<B> bTree, S overallMatching, Table<A, B, M> nodeMappings) {
        super();
        this.aTree = aTree;
        this.bTree = bTree;
        this.overallMatching = overallMatching;
        this.nodeMappings = nodeMappings;
    }

    public Tree<A> getaTree() {
        return aTree;
    }

    public Tree<B> getbTree() {
        return bTree;
    }

    public S getOverallMatching() {
        return overallMatching;
    }

    public Table<A, B, M> getNodeMappings() {
        return nodeMappings;
    }

    @Override
    public String toString() {
        return "TreeMapping [overallMatching=" + overallMatching + "]";
                //+ ", nodeMappings=" + nodeMappings + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((overallMatching == null) ? 0 : overallMatching.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeMapping other = (TreeMapping) obj;
		if (overallMatching == null) {
			if (other.overallMatching != null)
				return false;
		} else if (!overallMatching.equals(other.overallMatching))
			return false;
		return true;
	}
    
    
    
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((aTree == null) ? 0 : aTree.hashCode());
//		result = prime * result + ((bTree == null) ? 0 : bTree.hashCode());
//		result = prime * result + ((nodeMappings == null) ? 0 : nodeMappings.hashCode());
//		result = prime * result + ((overallMatching == null) ? 0 : overallMatching.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		TreeMapping<?, ?, ?, ?> other = (TreeMapping<?, ?, ?, ?>) obj;
//		if (aTree == null) {
//			if (other.aTree != null)
//				return false;
//		} else if (!aTree.equals(other.aTree))
//			return false;
//		if (bTree == null) {
//			if (other.bTree != null)
//				return false;
//		} else if (!bTree.equals(other.bTree))
//			return false;
//		if (nodeMappings == null) {
//			if (other.nodeMappings != null)
//				return false;
//		} else if (!nodeMappings.equals(other.nodeMappings))
//			return false;
//		if (overallMatching == null) {
//			if (other.overallMatching != null)
//				return false;
//		} else if (!overallMatching.equals(other.overallMatching))
//			return false;
//		return true;
//	}
   
}