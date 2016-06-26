package org.aksw.jena_sparql_api.utils;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

public class NodeTransformSignaturize
    implements NodeTransform
{
    protected NodeTransform baseTransform;
    protected Node placeholder;

    public NodeTransformSignaturize() {
        this((node) -> null);
    }

    public NodeTransformSignaturize(NodeTransform baseTransform) {
        this(baseTransform, Vars.signaturePlaceholder);
    }

    public NodeTransformSignaturize(NodeTransform baseTransform,
            Node placeholder) {
        super();
        this.baseTransform = baseTransform;
        this.placeholder = placeholder;
    }


    //public static NodeTransform createSignaturizeTransform(Expr expr, Map<? extends Node, ? extends Node> nodeMap) {

    @Override
    public Node apply(Node node) {
        Node remap = baseTransform.apply(node);

        Node result = remap == null || remap == node
                        ? (node.isVariable() ? placeholder : node)
                        : remap
                        ;

       return result;
    }

    public static NodeTransform create(Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform baseTransform = new NodeTransformRenameMap(nodeMap);
        NodeTransform result = new NodeTransformSignaturize(baseTransform);
        return result;
    }
}
