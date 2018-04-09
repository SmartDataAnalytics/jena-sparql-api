package org.aksw.jena_sparql_api.core.utils;

import java.util.Set;

import org.aksw.jena_sparql_api.utils.SetFromGraph;

import com.google.common.base.Function;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

public class FN_GraphToSet
    implements Function<Graph, Set<Triple>>
{
    @Override
    public Set<Triple> apply(Graph input) {
        Set<Triple> result = new SetFromGraph(input);
        return result;
    }

    public static final FN_GraphToSet fn = new FN_GraphToSet();
}
