package org.aksw.jena_sparql_api.geo;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class FunctionNodeValueToString
    implements Function<NodeValue, String>
{

    @Override
    public String apply(NodeValue nv) {
        String result = null;

        if(nv == null) {

        }
        else if(nv.isString()) {
            result = nv.asUnquotedString();
        }
        else if(nv.isLiteral()) {
            Node n = nv.asNode();
            if(n.isLiteral()) {
                result = n.getLiteralLexicalForm();
            }
        }

        return result;
    }
    
    
    public static final FunctionNodeValueToString fn = new FunctionNodeValueToString();
}
