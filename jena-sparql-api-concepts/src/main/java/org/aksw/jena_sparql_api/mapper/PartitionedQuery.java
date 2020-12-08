package org.aksw.jena_sparql_api.mapper;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.HasElement;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

/**
 * A query where a subset of the mentioned vars are used to partition
 * its result set. The partitioned result set can then serve as the
 * basis for accumulating objects from it - i.e. each partition is the base for an object
 * (which may be a set of objects).
 *
 * TODO Add a feature that allows specification of mappings of blank nodes in construct templates
 * to expressions that remap them to local IDs; i.e. Map<Node, Expr> nodeToIdExpr
 * Conceptually this substitutes bnodes with variables that are bound to an expression
 * CONSTRUCT { ?bnode1 a Foo } { ... ?x . BIND(fn(?x) AS ?bnode1 }
 * This way bnode substitution can be done explicitly in the client
 * Actually this is again the Sponate concept where objects are accumulated using aggregation based
 * on a JSON template (rather than CONSTRUCT/triples template). Back then we had even support to assemble lists from sparql results.
 *
 *
 *
 * @author raven
 *
 */
public interface PartitionedQuery
    extends HasElement
{
    Query getQuery();
    List<Var> getPartitionVars();

    default Template getTemplate() {
        Query query = getQuery();
        Template result = query.getConstructTemplate();
        return result;
    }

    @Override
    default Element getElement() {
        Query query = getQuery();
        Element result = query.getQueryPattern();
        return result;
    }
}
