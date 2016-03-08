package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TripletPath<V, E> {
    protected V start;
    protected V end;
    protected List<Triplet<V, E>> triplets;

    public TripletPath(V start, V end, List<Triplet<V, E>> triples) {
        super();
        this.start = start;
        this.end = end;
        this.triplets = triples;
    }

    /**
     * An rdf path is cycle free, if it contains each triple at most once
     * @return
     */
    public boolean isCycleFree() {
        int n = triplets.size();
        int m = (new HashSet<Triplet<V, E>>(triplets)).size();
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
        int result = triplets.size();
        return result;
    }

    public List<Triplet<V, E>> getTriplets() {
        return triplets;
    }

    public TripletPath<V, E> reverse() {
        List<Triplet<V, E>> tmp = new ArrayList<>(triplets);
        Collections.reverse(tmp);
        TripletPath<V, E> result = new TripletPath<>(end, start, triplets);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((triplets == null) ? 0 : triplets.hashCode());
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
        TripletPath<?, ?> other = (TripletPath<?, ?>) obj;
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
        if (triplets == null) {
            if (other.triplets != null)
                return false;
        } else if (!triplets.equals(other.triplets))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = "TripletPath[" + start + " -> " + end + ": " + start;

        V current = start;

        for(Triplet<V, E> t : triplets) {

            String dir;
            if(t.getSubject().equals(current)) {
                dir = "";
                current = t.getObject();
            } else if(t.getObject().equals(current)) {
                current = t.getSubject();
                dir = "^";
            } else {
                // disconnected triplet in the path, print out the new subject
                current = t.getObject();
                dir = "; " + t.getSubject() + " ";
            }

            result += " " + dir + t.getPredicate() + " " + current;
        }

        if(!current.equals(end)) {
            result += "; " + end;
        }

        result += "]";

        return result;
    }
}