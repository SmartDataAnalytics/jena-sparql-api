package org.aksw.jena_sparql_api.concept.builder.api;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprVar;

/**
 * Expression that can be used to refer to the set of values appearing as the filler
 * of a certain property restriction
 * 
 * @author raven
 *
 */
public class E_FillerRef
    extends ExprVar
{
    public E_FillerRef(Node n) {
        super(n);
    }

}
