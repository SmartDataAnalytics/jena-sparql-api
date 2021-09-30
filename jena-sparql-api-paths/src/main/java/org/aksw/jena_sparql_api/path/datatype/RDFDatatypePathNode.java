package org.aksw.jena_sparql_api.path.datatype;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.path.core.PathNode;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;


public class RDFDatatypePathNode
    extends RDFDatatypePathBase<Node, PathNode>
{
    public static final String IRI = "http://jsa.aksw.org/dt/sparql/pathNode";
    public static final RDFDatatypePathNode INSTANCE = new RDFDatatypePathNode();

    public RDFDatatypePathNode() {
        super(IRI, PathOpsNode.get());
    }


    public static Node createNode(Path<Node> path) {
        return NodeFactory.createLiteral(path.toString(), INSTANCE);
    }

    public static PathNode extractPath(Node node) {
        return RDFDatatypePathBase.extractPath(node);
    }
}
