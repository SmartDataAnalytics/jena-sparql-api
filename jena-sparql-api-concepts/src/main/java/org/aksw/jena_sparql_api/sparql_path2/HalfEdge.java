package org.aksw.jena_sparql_api.sparql_path2;

public class HalfEdge<V, E> {
    protected V startVertex;
    protected E edgeLabel;

    public HalfEdge(V startVertex, E edgeLabel) {
        super();
        this.startVertex = startVertex;
        this.edgeLabel = edgeLabel;
    }
    public V getStartVertex() {
        return startVertex;
    }
    public E getEdgeLabel() {
        return edgeLabel;
    }

    public static <V, E> HalfEdge<V, E> create(V startVertex, E edgeValue) {
        HalfEdge<V, E> result = new HalfEdge<V, E>(startVertex, edgeValue);
        return result;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((edgeLabel == null) ? 0 : edgeLabel.hashCode());
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
        HalfEdge other = (HalfEdge) obj;
        if (edgeLabel == null) {
            if (other.edgeLabel != null)
                return false;
        } else if (!edgeLabel.equals(other.edgeLabel))
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
        return "HalfEdge [startVertex=" + startVertex + ", edgeLabel="
                + edgeLabel + "]";
    }
}