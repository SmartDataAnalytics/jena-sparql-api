package org.aksw.jena_sparql_api.utils;

import java.util.Map;

import org.aksw.commons.collections.MapUtils;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

public class NodeTransformRenameMap
    implements NodeTransform {

    private final Map<?, ? extends Node> map;

    public NodeTransformRenameMap(Map<?, ? extends Node> map)
    {
        this.map = map;
    }

    public final Node apply(Node node)
    {
        Node result = MapUtils.getOrElse(map, node, node);
        return result;
    }
}
