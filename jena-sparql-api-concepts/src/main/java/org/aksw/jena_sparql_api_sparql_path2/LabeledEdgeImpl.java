package org.aksw.jena_sparql_api_sparql_path2;

import org.jgrapht.graph.DefaultEdge;

public class LabeledEdgeImpl<V, T>
    extends DefaultEdge
    implements LabeledEdge<V, T>
{
    private static final long serialVersionUID = 1L;

    protected V source;
    protected V target;
    protected T label;

    public LabeledEdgeImpl(V source, V target, T label) {
        super();
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public T getLabel() {
        return label;
    }

    public void setLabel(T label) {
        this.label = label;
    }

    public V getSource() {
        return source;
    }

    public V getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "LabeledEdgeImpl [source=" + source + ", target=" + target
                + ", label=" + label + "]";
    }

    public static <V, T> boolean isEpsilon(LabeledEdge<V, T> edge) {
    //public static boolean isEpsilon(LabeledEdge<?, ?> edge) {
        boolean result = edge.getLabel() == null;
        return result;
    }
}