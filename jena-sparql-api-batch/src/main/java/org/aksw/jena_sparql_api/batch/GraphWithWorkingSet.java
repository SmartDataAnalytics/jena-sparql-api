package org.aksw.jena_sparql_api.batch;

import org.aksw.jena_sparql_api.shape.ResourceShape;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.graph.GraphWrapper;

public class GraphWithWorkingSet
    extends GraphWrapper
{
    private final ResourceShape shape;
    
    public GraphWithWorkingSet(Graph graph, ResourceShape shape) {
        super(graph);
        this.shape = shape;
    }

    
    public Graph getValidGraph() {        
        
        return null;
    }
    
//    public boolean isValid() {
//        
//    }

//    @Override
//    public void performAdd( Triple t ) {
//        
//        throw new AddDeniedException( "GraphBase::performAdd" );
//    }
//
//    @Override
//    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//

    public static Graph getValidGraph(Graph g, ResourceShape s) {
        //shape.getOutgoing();
        
        return null;
    }

}
