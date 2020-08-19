package org.aksw.jena_sparql_api.sparql_path2;

public class PartialNfa<V, T> {
    //protected DirectedGraph<V, P_Link> graph;

    protected V startVertex;
    protected Iterable<HalfEdge<V, T>> looseEnds;

    public PartialNfa(V startVertex,
            Iterable<HalfEdge<V, T>> looseEnds) {
        super();
        this.startVertex = startVertex;
        this.looseEnds = looseEnds;
    }

    public V getStartVertex() {
        return startVertex;
    }

    public Iterable<HalfEdge<V, T>> getLooseEnds() {
        return looseEnds;
    }

    public static <V, T> PartialNfa<V, T> create(V startState, Iterable<HalfEdge<V, T>> looseEnds) {
        PartialNfa<V, T> result = new PartialNfa<V, T>(startState, looseEnds);
        return result;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((looseEnds == null) ? 0 : looseEnds.hashCode());
        result = prime * result
                + ((startVertex == null) ? 0 : startVertex.hashCode());
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
        PartialNfa<?, ?> other = (PartialNfa<?, ?>) obj;
        if (looseEnds == null) {
            if (other.looseEnds != null)
                return false;
        } else if (!looseEnds.equals(other.looseEnds))
            return false;
        if (startVertex == null) {
            if (other.startVertex != null)
                return false;
        } else if (!startVertex.equals(other.startVertex))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PartialNfa [startVertex=" + startVertex + ", looseEnds="
                + looseEnds + "]";
    }
}