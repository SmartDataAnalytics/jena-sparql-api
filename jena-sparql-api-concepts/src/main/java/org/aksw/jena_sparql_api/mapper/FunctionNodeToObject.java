package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

import org.apache.jena.graph.Node;

public class FunctionNodeToObject
    implements Function<Node, Object>
{
    @Override
    public Object apply(Node node) {
        Object result;

        if(node == null) {
            result = null;
        }
        else if(node.isURI()) {
            result = node.getURI();
        }
        else if(node.isLiteral()) {
            result = node.getLiteralValue();
        }
        else if(node.isBlank()){
            result = node.getBlankNodeLabel();
        }
        else {
            throw new RuntimeException("Unhandled case");
        }

        return result;
    }

    public static final FunctionNodeToObject fn = new FunctionNodeToObject();
}
