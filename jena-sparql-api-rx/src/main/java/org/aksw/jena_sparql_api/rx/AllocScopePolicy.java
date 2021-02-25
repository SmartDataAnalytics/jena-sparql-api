package org.aksw.jena_sparql_api.rx;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.MapWithScope.ScopePolicy;

/**
 * Public version of {@link org.apache.jena.riot.lang.LabelToNode.AllocScopePolicy}
 * Used for default blank node allocation in RdfDataMgrRx.
 */
public class AllocScopePolicy implements ScopePolicy<String, Node, Node>
{ 
    @Override
    public Map<String, Node> getScope(Node scope)   { return null ; }
    @Override
    public void clear() { }
}