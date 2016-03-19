package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A path from triplets. A path is expected to be connected.
 *
 * @author raven
 *
 * @param <V>
 * @param <E>
 */
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

    public Set<V> getNodeSet() {
        Set<V> result = new HashSet<>();
        result.add(start);
        for(Triplet<V, E> t : triplets) {
            result.add(t.getSubject());
            result.add(t.getObject());
        }
        result.add(end);
        return result;
    }

    //a [t1] b [t2] c
    public V getNode(int i) {
        V result;
        int n = triplets.size();
        if(i == 0) {
            result = start;
        } else if(i == n) { // note: if there are no triplets, start and end are expected to be equal
            result = end;
        } else if(i < n) {
            Triplet<V, E> tmp = triplets.get(i);
            result = tmp.getSubject();
        } else {
            throw new IndexOutOfBoundsException(i + " must not exceed " + n);
        }

        return result;
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

    public TripletPath<V, E> subPath(int fromIndex, int toIndex) {
        TripletPath<V, Directed<E>> tmp = TripletPath.makeDirected(this);
        V s = tmp.getNode(fromIndex);
        List<Triplet<V, Directed<E>>> ts = tmp.getTriplets().subList(fromIndex, toIndex);
        V e = ts.isEmpty() ? s : ts.get(ts.size() - 1).getObject();


        List<Triplet<V, E>> newTriplets = makeUndirected(ts);
        TripletPath<V, E> result = new TripletPath<>(s, e, newTriplets);
        return result;
    }

    public V getStart() {
        return start;
    }

    public V getEnd() {
        return end;
    }

    /**
     * Returns the number of triplets - NOT nodes
     *
     * @return
     */
    public int getLength() {
        int result = triplets.size();
        return result;
    }

    // maybe: get node list
//    public int getNodeLength() {
//        int n = triplets.size();
//        int result = triple
//    }

    public List<Triplet<V, E>> getTriplets() {
        return triplets;
    }

    public static <V, E> List<Triplet<V, E>> makeUndirected(List<Triplet<V, Directed<E>>> triplets) {
        List<Triplet<V, E>> result = triplets.stream()
                .map(Triplet::makeUndirected)
                .collect(Collectors.toList());

        return result;
    }

    public static <V, E> TripletPath<V, E> makeUndirected(TripletPath<V, Directed<E>> path) {
        List<Triplet<V, E>> tmp = makeUndirected(path.getTriplets());

        TripletPath<V, E> result = new TripletPath<>(path.getStart(), path.getEnd(), tmp);
        return result;
    }

    public static <V, E> TripletPath<V, Directed<E>> makeDirected(TripletPath<V, E> path) {
        V s = path.getStart();
        List<Triplet<V, E>> triplets = path.getTriplets();
        List<Triplet<V, Directed<E>>> newTriplets = new ArrayList<>(triplets.size());

        for(Triplet<V, E> t : triplets) {
            V o;
            boolean reverse;
            if(t.getSubject().equals(s)) {
                reverse = false;
                o = t.getObject();
            } else if(t.getObject().equals(s)) {
                o = t.getSubject();
                reverse = true;
            } else {
                // disconnected triplet in the path, print out the new subject
                o = t.getObject();
                //dir = "; " + t.getSubject() + " ";
                reverse = false;
            }

            Directed<E> p = new Directed<>(t.getPredicate(), reverse);
            newTriplets.add(new Triplet<>(s, p, o));
            s = o;
        }

        TripletPath<V, Directed<E>> result = new TripletPath<>(path.getStart(), path.getEnd(), newTriplets);
        return result;
    }


    public TripletPath<V, E> reverse() {
        List<Triplet<V, E>> tmp = new ArrayList<>(triplets);
        Collections.reverse(tmp);
        TripletPath<V, E> result = new TripletPath<>(end, start, triplets);
        return result;
    }


    public TripletPath<V, E> concat(TripletPath<V, E> that) {
        List<Triplet<V, E>> triplets = new ArrayList<>(this.triplets.size() + that.triplets.size());
        triplets.addAll(this.triplets);
        triplets.addAll(that.triplets);
        TripletPath<V, E> result = new TripletPath<V, E>(start, that.end, triplets);
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