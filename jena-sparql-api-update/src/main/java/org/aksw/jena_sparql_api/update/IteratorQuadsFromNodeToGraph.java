package org.aksw.jena_sparql_api.update;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.AbstractIterator;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class IteratorQuadsFromNodeToGraph
    extends AbstractIterator<Quad>
{
    private Iterator<Entry<Node, Graph>> itGraphs;

    private Entry<Node, Graph> currentEntry;
    private ExtendedIterator<Triple> itTriples;


    public IteratorQuadsFromNodeToGraph(Iterator<Entry<Node, Graph>> itGraphs) {
        this.itGraphs = itGraphs;
    }

    @Override
    protected Quad computeNext() {
        while(itTriples == null || !itTriples.hasNext()) {
            for(;;) {
                if(itGraphs.hasNext()) {
                    currentEntry = itGraphs.next();
                    break;

//                    if(currentEntry == null) {
//                        continue;
//                    }
                } else {
                    return endOfData();
                }
            }

            itTriples = currentEntry.getValue().find(Node.ANY,Node.ANY, Node.ANY);
        }

        Node g = currentEntry.getKey();
        Triple triple = itTriples.next();
        Quad result = new Quad(g, triple);
        return result;
    }

    public static IteratorQuadsFromNodeToGraph create(Map<Node, Graph> nodeToGraph) {
        IteratorQuadsFromNodeToGraph result = new IteratorQuadsFromNodeToGraph(nodeToGraph.entrySet().iterator());
        return result;
    }

}