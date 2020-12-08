package org.aksw.jena_sparql_api.mapper;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

/**
 * An object (graph) query is an extension of a SPARQL construct query:
 * Blank nodes in the query template can additionally be mapped to sequences
 * of SPARQL expressions. This way blank nodes can be assigned a client-side mapping
 * that allocate specific ID thus enabling deterministic template instantiation without
 * the need of having the ID generation part of the SPARQL query.
 *
 * Yet, if desired, the use of SPARQL expressions enables query rewriting
 * that pushes the ID generation into a conventional SPARQL query as illustrated below:
 *
 * <pre>
 * CONSTRUCT {
 *   ?bnode a FacetValueCount ; facet ?f ; value ?v ; count ?c
 * } WHERE {
 *   SELECT { ?f ?v ?c { ... } }
 *   BIND(sequence(?f, ?v) AS ?bnode)
 * </pre>
 *
 *
 * @author raven
 *
 */
public interface ObjectQuery {
    /**
     * The template is the same as for SPARQL contstruct queries
     *
     * @return The template
     */
    Template getTemplate();

    /**
     * The mapping of blank nodes in the template to expression lists.
     * Only blank nodes should be mapped.
     * Any expression in the ExprList should only make use of variables that
     * are distinguished variables of the relation
     *
     * @return The mapping. May be empty but never null.
     */
    Map<Node, ExprList> getIdMapping();

    /**
     * Relation is a uniform representation for something that is or can be evaluated
     * to a set of {@link Binding}s.
     * Concretely, a relation can be (SELECT) {@link Query}, {@link Element} or {@link Table}.
     *
     * @return The relation that acts as the source of bindings for this object query
     */
    Relation getRelation();
}
