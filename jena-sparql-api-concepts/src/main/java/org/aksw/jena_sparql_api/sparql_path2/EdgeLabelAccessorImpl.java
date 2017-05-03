package org.aksw.jena_sparql_api.sparql_path2;

import org.aksw.jena_sparql_api.jgrapht.wrapper.LabeledEdge;

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