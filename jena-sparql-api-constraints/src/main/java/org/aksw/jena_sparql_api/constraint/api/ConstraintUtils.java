package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jena_sparql_api.constraint.util.RdfTermType;
import org.apache.jena.graph.Node;

public class ConstraintUtils {
    public static RdfTermType getRdfTermType(Node node) {
        if(node == null) {
            return RdfTermType.UNKNOWN;
        } else if(node.isURI()) {
            return RdfTermType.IRI;
        } else if(node.isLiteral()) {
            return RdfTermType.LITERAL;
        } else if(node.isBlank()) {
            return RdfTermType.BNODE;
        } else {
            throw new RuntimeException("Should not happen");
        }
    }

}
