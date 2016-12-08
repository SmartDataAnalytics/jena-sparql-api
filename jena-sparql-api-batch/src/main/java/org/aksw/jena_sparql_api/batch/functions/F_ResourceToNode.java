package org.aksw.jena_sparql_api.batch.functions;

import com.google.common.base.Function;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

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
