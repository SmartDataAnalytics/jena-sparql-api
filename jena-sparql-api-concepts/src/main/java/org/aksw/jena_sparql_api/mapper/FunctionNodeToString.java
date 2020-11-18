package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

import org.apache.jena.graph.Node;

public class FunctionNodeToString
    implements Function<Node, String>
{
    @Override
    public String apply(Node node) {
        String result;

        if(node == null) {
            result = null;
        }
        else if(node.isURI()) {
            result = node.getURI();
        }
        else if(node.isLiteral()) {
            result = node.getLiteralLexicalForm();
        }
        else if(node.isBlank()){
            result = node.getBlankNodeLabel();
        }
        else {
            throw new RuntimeException("Unhandled case");
        }

        return result;
    }

    public static final FunctionNodeToString fn = new FunctionNodeToString();
}
