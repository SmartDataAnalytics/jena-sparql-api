package org.aksw.jena_sparql_api.rx.entity.model;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;

/**
 * A helper interface to abstract the concrete method by which
 * a Node is computed from a list of expressions w.r.t. a Binding.
 *
 * This method is used to allocate nodes
 *
 * @author raven
 *
 */
public interface ExprListEval {
    Node eval(ExprList el, Binding binding);
}