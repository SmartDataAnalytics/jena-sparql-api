package org.aksw.jena_sparql_api.utils.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TripletImpl<V, E>
    implements Triplet<V, E>
{
    protected V subject;
    protected E predicate;
    protected V object;

    public TripletImpl(V subject, E predicate, V object) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public static <V, E> Triplet<V, Directed<E>> makeDirected(Triplet<V, E> in, V source) {
        int dir = getDirection(in, source);
        Triplet<V, Directed<E>> result = (dir & 1) != 0
                ? new TripletImpl<>(in.getSubject(), new Directed<>(in.getPredicate(), false), in.getObject())
                : (dir & 2) != 0
                    ? new TripletImpl<>(in.getObject(), new Directed<>(in.getPredicate(), true), in.getSubject())
                    : null
                ;

        if(result == null) {
            throw new RuntimeException("Should not happen");
        }

        return result;
    }

    public static <V, E> Triplet<V, E> makeUndirected(Triplet<V, Directed<E>> in) {
        Directed<E> dp = in.getPredicate();
        Triplet<V, E> result = dp.isReverse()
                ? new TripletImpl<>(in.getObject(), dp.getValue(), in.getSubject())
                : new TripletImpl<>(in.getSubject(), dp.getValue(), in.getObject())
                ;

        return result;
    }



    public static <V, E> Triplet<V, E> create(V s, E e, V o, boolean reverse) {
        Triplet<V, E> result = !reverse
                ? new TripletImpl<>(s, e, o)
                : new TripletImpl<>(o, e, s)
                ;

        return result;
    }

    public static <V> V getSource(Triplet<V, ?> triplet, boolean reverse) {
        V result = reverse
                ? triplet.getObject()
                : triplet.getSubject()
                ;

        return result;
    }

    public static <V> V getTarget(Triplet<V, ?> triplet, boolean reverse) {
        V result = getSource(triplet, !reverse);
        return result;
    }

    public static <V> V getTarget(Triplet<V, ?> triplet, Object source) {
        V result = triplet.getSubject().equals(source)
                ? triplet.getObject()
                : triplet.getSubject()
                ;
        return result;
    }

    /**
     * Return a list of indices based on whether the given node is equal to the triple's subject and/or object.
     *
     * @param triplet
     * @param node
     * @return
     */
    public static List<Boolean> getDirections(Triplet<?, ?> triplet, Object node) {
        int dir = getDirection(triplet, node);
        List<Boolean> result;
        switch(dir) {
        case 0: result = Collections.emptyList(); break;
        case 1: result = Collections.singletonList(false); break;
        case 2: result = Collections.singletonList(true); break;
        case 3: result = Arrays.asList(false, true); break;
        default: throw new RuntimeException("Should not happen");
        }
        return result;
    }


//    public static int[] getDirections(Triplet<?, ?> triplet, Object node) {
//        // TODO Make static constants
//        int empty[] = new int[] {};
//        int zero[] = new int[] {0};
//        int one[] = new int[] {1};
//        int zeroOrOne[] = new int[] {0, 1};
//
//        int dir = getDirection(triplet, node);
//        int[] result;
//        switch(dir) {
//        case 0: result = empty; break;
//        case 1: result = zero; break;
//        case 2: result = one; break;
//        case 3: result = zeroOrOne; break;
//        default: throw new RuntimeException("Should not happen");
//        }
//        return result;
//    }


    /**
     * Returns a bit mask:
     * index: 1        0
     *        [bwdBit] [fwdBit]
     *
     * The first bit indicates whether the subject was equal to the given node,
     * the second bit whether the object was equal to the given node.
     *
     * Possible values are therefore 0 (no match), 3 (both subject and object matched) or 1 and 2.
     *
     * @param triplet
     * @param node
     * @return
     */
    public static int getDirection(Triplet<?, ?> triplet, Object node) {
        int result =
                (triplet.getSubject().equals(node) ? 1 : 0) |
                (triplet.getObject().equals(node) ? 2 : 0);

        return result;
    }


    public V getSubject() {
        return subject;
    }

    public E getPredicate() {
        return predicate;
    }

    public V getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result
                + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
        TripletImpl<?, ?> other = (TripletImpl<?, ?>) obj;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Triplet [subject=" + subject + ", predicate=" + predicate
                + ", object=" + object + "]";
    }
}
