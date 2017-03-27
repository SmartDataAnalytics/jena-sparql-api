package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jena_sparql_api.utils.Vars;

public class NodeBuilderFluent {
    public static NodeBuilder start() {
        NodeBuilder result = new NodeBuilderImpl(Vars.s);
        return result;
    }
}
