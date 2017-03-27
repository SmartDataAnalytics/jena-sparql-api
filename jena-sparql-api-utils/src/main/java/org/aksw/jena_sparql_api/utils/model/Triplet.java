package org.aksw.jena_sparql_api.utils.model;

public interface Triplet<V, E> {
    V getSubject();
    E getPredicate();
    V getObject();

    public static <V, E> Triplet<V, E> swap(Triplet<V, E> t) {
        Triplet<V, E> result = new TripletImpl<>(t.getObject(), t.getPredicate(), t.getSubject());
        return result;
    }

}
