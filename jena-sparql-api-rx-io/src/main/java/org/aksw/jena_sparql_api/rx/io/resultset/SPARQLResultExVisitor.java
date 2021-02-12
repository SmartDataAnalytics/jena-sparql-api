package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.Iterator;

import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public interface SPARQLResultExVisitor<T>
    extends SPARQLResultVisitor<T> {

    T onQuads(Iterator<Quad> it);
    T onTriples(Iterator<Triple> it);

    default T forwardEx(SPARQLResultEx sr) {
        T result;

        if (sr.isTriples()) {
            result = onTriples(sr.getTriples());
        } else if (sr.isQuads()) {
            result = onQuads(sr.getQuads());
        } else if (sr.isUpdateType()) {
            // nothing to do
            result = null;
        } else {
            result = forward(sr);
        }

        return result;
    }
}