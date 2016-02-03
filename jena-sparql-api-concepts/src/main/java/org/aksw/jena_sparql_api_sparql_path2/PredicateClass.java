package org.aksw.jena_sparql_api_sparql_path2;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public class PredicateClass
{
    protected ValueSet<Node> fwdNodes;
    protected ValueSet<Node> bwdNodes;

    public PredicateClass(ValueSet<Node> fwdNodes, ValueSet<Node> bwdNodes) {
        super();
        this.fwdNodes = fwdNodes;
        this.bwdNodes = bwdNodes;
    }

    public ValueSet<Node> getFwdNodes() {
        return fwdNodes;
    }

    public ValueSet<Node> getBwdNodes() {
        return bwdNodes;
    }

    public boolean contains(P_Path0 path) {
        Node node = path.getNode();
        boolean result = path.isForward()
            ? fwdNodes.contains(node)
            : bwdNodes.contains(node)
            ;
       return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((bwdNodes == null) ? 0 : bwdNodes.hashCode());
        result = prime * result
                + ((fwdNodes == null) ? 0 : fwdNodes.hashCode());
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
        PredicateClass other = (PredicateClass) obj;
        if (bwdNodes == null) {
            if (other.bwdNodes != null)
                return false;
        } else if (!bwdNodes.equals(other.bwdNodes))
            return false;
        if (fwdNodes == null) {
            if (other.fwdNodes != null)
                return false;
        } else if (!fwdNodes.equals(other.fwdNodes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PredicateClass [fwdNodes=" + fwdNodes + ", bwdNodes=" + bwdNodes
                + "]";
    }

}