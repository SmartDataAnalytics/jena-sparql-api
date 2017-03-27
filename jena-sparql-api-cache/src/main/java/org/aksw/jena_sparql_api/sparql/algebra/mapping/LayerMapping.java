package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.List;
import java.util.Map;

import org.aksw.combinatorics.collections.NodeMapping;

public class LayerMapping<A, B, S>
    //implements Iterable<NodeMapping<A, B, S>>
{
    protected List<NodeMapping<A, B, S>> nodeMappings;
    protected Map<A, B> parentMap;
    
//    public LayerMapping() {
//        super();
//        this.parentMap = new 
//        this.nodeMappings = new ArrayList<>();
//    }
//

    public LayerMapping(List<NodeMapping<A, B, S>> nodeMappings, Map<A, B> parentMap) {
        super();
        this.nodeMappings = nodeMappings;
        this.parentMap = parentMap;
    }

    public Map<A, B> getParentMap() {
        return parentMap;
    }

    public List<NodeMapping<A, B, S>> getNodeMappings() {
        return nodeMappings;
    }

    @Override
    public String toString() {
        return "LayerMapping [nodeMappings=" + nodeMappings + ", parentMap="
                + parentMap + "]";
    }
    
}
