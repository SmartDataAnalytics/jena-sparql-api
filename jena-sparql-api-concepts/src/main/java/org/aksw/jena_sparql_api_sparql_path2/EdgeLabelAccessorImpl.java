package org.aksw.jena_sparql_api_sparql_path2;

public class EdgeLabelAccessorImpl<V, E, T>
    implements EdgeLabelAccessor<LabeledEdge<V, T>, T>
{
    @Override
    public T getLabel(LabeledEdge<V, T> edge) {
        T result = edge.getLabel();
        return result;
    }

    @Override
    public void setLabel(LabeledEdge<V, T> edge, T label) {
        edge.setLabel(label);
    }
}