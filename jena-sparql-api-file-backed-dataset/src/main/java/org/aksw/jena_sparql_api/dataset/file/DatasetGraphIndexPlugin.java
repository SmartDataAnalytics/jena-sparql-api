package org.aksw.jena_sparql_api.dataset.file;

import java.util.Iterator;

import org.apache.jena.graph.Node;

public interface DatasetGraphIndexPlugin {
    public Float evaluateFind(Node s, Node p, Node o);

    /**
     * If the result of {{@link #evaluateFind(Node, Node, Node)} is non-null then
     * this method is expected to yield an iterator of the graph nodes which may contain
     * triples matching the arguments.
     *
     * @param s
     * @param p
     * @param o
     * @return
     */
    public Iterator<Node> listGraphNodes(Node s, Node p, Node o);

    public void add(Node g, Node s, Node p, Node o);
    public void delete(Node g, Node s, Node p, Node o);
}
