package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;

public class NestedRdfPath {
    protected NestedRdfPath parent;
    protected Node parentProperty; // only valid if parent is not null
    protected boolean isReverse;
    protected Node current; // the node reached by this path

    public NestedRdfPath(Node current) {
        this(null, null, false, current);
    }

    public NestedRdfPath(NestedRdfPath parent, P_Path0 path, Node current) {
        this(parent, path.getNode(), ! path.isForward(), current);
    }

    public NestedRdfPath(NestedRdfPath parent, Node parentProperty,
            boolean isReverse, Node current) {
        super();
        this.parent = parent;
        this.parentProperty = parentProperty;
        this.isReverse = isReverse;
        this.current = current;
    }

    public NestedRdfPath getParent() {
        return parent;
    }
    public Node getParentProperty() {
        return parentProperty;
    }
    public boolean isReverse() {
        return isReverse;
    }
    public Node getCurrent() {
        return current;
    }

    public boolean isCycleFree() {
        RdfPath rdfPath = asSimplePath();
        boolean result = rdfPath.isCycleFree();
        return result;
    }

    public RdfPath asSimplePath() {
        Node end = current;

        NestedRdfPath c = this;
        Node start = end;
        List<Triple> triples = new ArrayList<Triple>();
        while(c != null) {
            Node o = c.getCurrent();
            NestedRdfPath pr = c.getParent();

            if(pr == null) {
                start = o;
            } else {
                Node p = c.getParentProperty();
                Node s = pr.getCurrent();

                Triple triple = new Triple(s, p, o);
                if(c.isReverse()) {
                    triple = TripleUtils.swap(triple);
                }

                triples.add(triple);
            }
            c = c.getParent();
        }

        Collections.reverse(triples);
        RdfPath result = new RdfPath(start, end, triples);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((current == null) ? 0 : current.hashCode());
        result = prime * result + (isReverse ? 1231 : 1237);
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result
                + ((parentProperty == null) ? 0 : parentProperty.hashCode());
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
        NestedRdfPath other = (NestedRdfPath) obj;
        if (current == null) {
            if (other.current != null)
                return false;
        } else if (!current.equals(other.current))
            return false;
        if (isReverse != other.isReverse)
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (parentProperty == null) {
            if (other.parentProperty != null)
                return false;
        } else if (!parentProperty.equals(other.parentProperty))
            return false;
        return true;
    }

    @Override
    public String toString() {
        RdfPath tmp = asSimplePath();
        return "NestedRdfPath: " + tmp.toString();
//        return "NestedRdfPath [parent=" + parent + ", parentProperty="
//                + parentProperty + ", isReverse=" + isReverse + ", current="
//                + current + "]";
    }



}
