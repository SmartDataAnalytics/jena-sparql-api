package org.aksw.jena_sparql_api.batch;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;

public class F_ResourceToNode
    implements Function<Resource, Node>
{
    @Override
    public Node apply(Resource input) {
        Node result = input.asNode();
        return result;
    }

    public static final F_ResourceToNode fn = new F_ResourceToNode();
}
