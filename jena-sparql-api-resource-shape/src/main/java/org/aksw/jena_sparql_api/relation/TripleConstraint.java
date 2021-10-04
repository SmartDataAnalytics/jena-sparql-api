package org.aksw.jena_sparql_api.relation;

import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.expr.Expr;

/**
 * This class captures a single triple pattern with a filter expression
 * which in algebraic terms corresponds to OpFilter(OpTriple(?s ?p ?o), expr).
 * The expression may only refer to variables mentioned in the pattern.
 * The pattern may make use of variables and concrete rdf terms. Hence, semantically equivalent constraints
 * can be expressed in different ways - e.g. { ?s ?p ?o . FILTER (?p = CONST) } == { ?s CONST ?o }
 *
 * This class can act as a predicate that can be evaluated for given individual
 * (concrete) triples.
 * The sparql expression allows obtaining the set of matching triples
 * for any a local or remote RDF graph.
 *
 */
public interface TripleConstraint
    extends Predicate<Triple>
{
    /**
     * A possibly (less selective) representation of the constraint as a match triple suitable for
     * pre-filtering using {@link Graph#find(Triple)}
     *
     * Must never return null. The most unselective triple pattern
     * is Triple.createMatch(null, null, null).
     *
     * @return
     */
    Triple getMatchTriple();

    /**
     * True if {@link #getMatchTriple()} matchs the same set of triples as {@link #getExpr()}.
     */
    boolean isMatchTripleExhaustive();

    /** An expression which only allows a subset of the variables ?s ?p and ?o */
    Expr getExpr();
}
