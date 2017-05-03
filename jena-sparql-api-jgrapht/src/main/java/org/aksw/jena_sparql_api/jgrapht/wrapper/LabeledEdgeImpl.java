package org.aksw.jena_sparql_api.jgrapht.wrapper;

import org.jgrapht.graph.DefaultEdge;

/**
 * TODO This class could make use of Triplet (although Triplet should probably not be tied to jGraphT and thus should not inherit from DefaultEdge)
 *
 * @author raven
 *
 * @param <V>
 * @param <T>
 */
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
        return "(" + source + ", " + target + ", " + label + ")";
    }

    public static <V, T> boolean isEpsilon(LabeledEdge<V, T> edge) {
    //public static boolean isEpsilon(LabeledEdge<?, ?> edge) {
        boolean result = edge.getLabel() == null;
        return result;
    }
}