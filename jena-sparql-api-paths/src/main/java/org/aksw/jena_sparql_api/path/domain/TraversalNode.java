package org.aksw.jena_sparql_api.path.domain;

import org.aksw.commons.path.trav.api.Trav;
import org.apache.jena.graph.Node;

public interface TraversalNode<T extends TraversalNode<T>>
    extends Trav<Node, T>
{
//    @Override
//    PathNode getPath();
}
