package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Path0;

public class NestedPath<V, E> {
    protected ParentLink<V, E> parentLink;
    protected V current; // the node reached by this path


    public NestedPath(V current) {
        this(null, current);
    }

    public NestedPath(ParentLink<V, E> parentLink, V current) {
        super();
        this.parentLink = parentLink;
        this.current = current;
    }

    public ParentLink<V, E> getParentLink() {
        return parentLink;
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

    public MyPath<V, E> asSimplePath() {
        V end = current;

        NestedPath<V, E> c = this;
        V start = end;
        List<Triplet<V, E>> triples = new ArrayList<>();
        while(c != null) {
            V o = c.getCurrent();
            //NestedRdfPath<V, E> pr = c.getParent();
            ParentLink<V, E> pr = c.getParentLink();

            if(pr == null) {
                start = o;
                c = null;
            } else {
                DirectedProperty<E> diProperty = pr.getDiProperty();

                E p = diProperty.getProperty();
                V s = pr.getTarget().getCurrent();

                Triplet<V, E> triple = new Triplet<>(s, p, o);
                if(diProperty.isReverse()) {
                    triple = Triplet.swap(triple);
                }

                triples.add(triple);
                c = pr.getTarget();
            }
        }

        Collections.reverse(triples);
        MyPath<V, E> result = new MyPath<>(start, end, triples);
        return result;
    }



}
