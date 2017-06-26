package org.aksw.jena_sparql_api.jgrapht.transform;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;

public interface GraphVar
    extends Graph
{
    BiMap<Var, Node> getVarToNode();

    // The underlying graph without the variable substitutions
    Graph getWrapped();

    default BiMap<Node, Var> getNodeToVar() {
        return getVarToNode().inverse();
    }
}
