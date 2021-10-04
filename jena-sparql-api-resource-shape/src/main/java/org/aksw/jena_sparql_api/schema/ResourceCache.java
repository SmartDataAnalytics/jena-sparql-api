package org.aksw.jena_sparql_api.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceState;
import org.apache.jena.graph.Node;

public class ResourceCache {
    protected Map<Node, ResourceState> srcToState = new LinkedHashMap<>();

    public ResourceState get(Node src) {
        return srcToState.get(src);
    }

    public ResourceState getOrCreate(Node src) {
        return srcToState.computeIfAbsent(src, ss -> new ResourceState(ss));
    }


    public Map<Node, ResourceState> getMap() {
        return srcToState;
    }
}
