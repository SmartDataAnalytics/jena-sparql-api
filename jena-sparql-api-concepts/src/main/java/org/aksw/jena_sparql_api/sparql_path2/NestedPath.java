package org.aksw.jena_sparql_api.sparql_path2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.aksw.jena_sparql_api.utils.model.Directed;
import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletImpl;
import org.aksw.jena_sparql_api.utils.model.TripletPath;

public class NestedPath<V, E>
    implements Serializable
{
    private static final long serialVersionUID = 8761194530191829164L;


    //protected Optional<ParentLink<V, E>> parentLink;
    protected ParentLink<V, E> parentLink;
    protected V current; // the node reached by this path


    public NestedPath(V current) {
        this(Optional.empty(), current);
    }

    public boolean containsEdge(Object edge, boolean reverse) {
        boolean result;
        if(parentLink != null) {
            Directed<E> pred = parentLink.getDiProperty();
            //E pred = parentLink.getDiProperty();
            result = reverse == pred.isReverse() && edge.equals(pred.getValue())
                    ? true
                    : parentLink.getTarget().containsEdge(edge, reverse);
        } else {
            result = false;
        }
        return result;
    }

    public NestedPath(Optional<ParentLink<V, E>> parentLink, V current) {
        this(parentLink.isPresent() ? parentLink.get() : null, current);
    }

    public NestedPath(ParentLink<V, E> parentLink, V current) {
        super();
        this.parentLink = parentLink;
        this.current = current;
    }


    public Optional<ParentLink<V, E>> getParentLink() {
        return Optional.ofNullable(parentLink);
    }

    public V getCurrent() {
        return current;
    }

    public boolean isCycleFree() {
        boolean result = asSimplePath().isCycleFree();
        return result;
    }

    public int getLength() {
        int result = asSimplePath().getLength();
        return result;
    }


    public TripletPath<V, Directed<E>> asSimpleDirectedPath() {
        V end = current;

        NestedPath<V, E> c = this;
        V start = end;
        List<Triplet<V, Directed<E>>> triplets = new ArrayList<>();
        while(c != null) {
            V o = c.getCurrent();
            //NestedRdfPath<V, E> pr = c.getParent();
            Optional<ParentLink<V, E>> opl = c.getParentLink();

            if(!opl.isPresent()) {
                start = o;
                c = null;
            } else {
                ParentLink<V, E> parentLink = opl.get();
                Directed<E> p = parentLink.getDiProperty();
                V s = parentLink.getTarget().getCurrent();

                Triplet<V, Directed<E>> triplet = new TripletImpl<>(s, p, o);

                triplets.add(triplet);
                c = parentLink.getTarget();
            }
        }

        Collections.reverse(triplets);
        TripletPath<V, Directed<E>> result = new TripletPath<>(start, end, triplets);
        return result;
    }

    public TripletPath<V, E> asSimplePath() {
        V end = current;

        NestedPath<V, E> c = this;
        V start = end;
        List<Triplet<V, E>> triples = new ArrayList<>();
        while(c != null) {
            V o = c.getCurrent();
            //NestedRdfPath<V, E> pr = c.getParent();
            Optional<ParentLink<V, E>> opl = c.getParentLink();

            if(!opl.isPresent()) {
                start = o;
                c = null;
            } else {
                ParentLink<V, E> parentLink = opl.get();
                Directed<E> diProperty = parentLink.getDiProperty();
                //E p = parentLink.getDiProperty();

                E p = diProperty.getValue();
                V s = parentLink.getTarget().getCurrent();

                Triplet<V, E> triple = new TripletImpl<>(s, p, o);
//                if(diProperty.isReverse()) {
//                    triple = Triplet.swap(triple);
//                }

                triples.add(triple);
                c = parentLink.getTarget();
            }
        }

        Collections.reverse(triples);
        TripletPath<V, E> result = new TripletPath<>(start, end, triples);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((current == null) ? 0 : current.hashCode());
        result = prime * result
                + ((parentLink == null) ? 0 : parentLink.hashCode());
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
        NestedPath<?, ?> other = (NestedPath<?, ?>) obj;
        if (current == null) {
            if (other.current != null)
                return false;
        } else if (!current.equals(other.current))
            return false;
        if (parentLink == null) {
            if (other.parentLink != null)
                return false;
        } else if (!parentLink.equals(other.parentLink))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NestedPath [parentLink=" + parentLink + ", current=" + current
                + "]";
    }

}
