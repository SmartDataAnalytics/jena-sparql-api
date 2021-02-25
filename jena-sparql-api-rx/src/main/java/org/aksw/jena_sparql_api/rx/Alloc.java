package org.aksw.jena_sparql_api.rx;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.lang.BlankNodeAllocator;
import org.apache.jena.riot.system.MapWithScope.Allocator;

/**
 * Public version of {@link org.apache.jena.riot.lang.LabelToNode.Alloc}
 * Used for default blank node allocation in RdfDataMgrRx.
 */
class Alloc implements Allocator<String, Node, Node> {
    final BlankNodeAllocator alloc ;
    
    Alloc(BlankNodeAllocator alloc)     { this.alloc = alloc ; }
    
    @Override
    public Node alloc(Node scope, String label)     { return alloc.alloc(label) ; }

    @Override
    public Node create()                { return alloc.create() ; }

    @Override
    public void reset()                 { alloc.reset() ; }
}