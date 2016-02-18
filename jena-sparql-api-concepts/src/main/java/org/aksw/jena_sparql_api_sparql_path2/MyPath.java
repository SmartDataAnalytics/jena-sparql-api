package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class MyPath<V, E> {
    protected V start;
    protected V end;
    protected List<Triplet<V, E>> triples;

    public MyPath(V start, V end, List<Triplet<V, E>> triples) {
        super();
        this.start = start;
        this.end = end;
        this.triples = triples;
    }

    /**
     * An rdf path is cycle free, if it contains each triple at most once
     * @return
     */
    public boolean isCycleFree() {
        int n = triples.size();
        int m = (new HashSet<Triplet<V, E>>(triples)).size();
        boolean result = n == m;
        return result;
    }

    public V getStart() {
        return start;
    }

    public V getEnd() {
        return end;
    }

    public int getLength() {
        int result = triples.size();
        return result;
    }

    public List<Triplet<V, E>> getTriples() {
        return triples;
    }

    public MyPath<V, E> reverse() {
        List<Triplet<V, E>> tmp = new ArrayList<>(triples);
        Collections.reverse(tmp);
        MyPath<V, E> result = new MyPath<>(end, start, triples);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((triples == null) ? 0 : triples.hashCode());
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
        MyPath other = (MyPath) obj;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (triples == null) {
            if (other.triples != null)
                return false;
        } else if (!triples.equals(other.triples))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RdfPath [start=" + start + ", end=" + end + ", triples="
                + triples + "]";
    }
}