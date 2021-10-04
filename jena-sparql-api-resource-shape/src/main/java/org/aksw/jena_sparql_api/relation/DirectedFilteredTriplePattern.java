package org.aksw.jena_sparql_api.relation;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.expr.ExprList;


/**
 * A single triple pattern combined with a filter and a direction.
 * Corresponds to a specification of a set of triples which can be executed via
 * {@code Stream.stream(graph.find(triplePattern)).filter(filter::test)}.
 *
 *
 * @author raven
 *
 */
public class DirectedFilteredTriplePattern {
    protected Triple triplePattern;

    /**
     * A conjunction of expressions
     */
    protected ExprList exprs;

    /** If isForward is true then the subject acts as the source and the object as the target.
     * otherwise its vice versa.
     */
    protected boolean isForward;

    public DirectedFilteredTriplePattern(Triple triplePattern, ExprList exprs, boolean isForward) {
        super();
        this.triplePattern = triplePattern;
        this.exprs = exprs;
        this.isForward = isForward;
    }

    public static DirectedFilteredTriplePattern create(Node source, Node predicate, boolean isForward) {
        return new DirectedFilteredTriplePattern(Triple.create(source, predicate, Vars.o), null, isForward);
    }

    public Node getSource() {
        return TripleUtils.getSource(triplePattern, isForward);
    }

    public Node getTarget() {
        return TripleUtils.getTarget(triplePattern, isForward);
    }

    public Triple getTriplePattern() {
        return triplePattern;
    }

    public ExprList getExprs() {
        return exprs;
    }

    public boolean isForward() {
        return isForward;
    }

    /**
     * Convert this object to a slightly simplified representation which loses the 'direction' information.
     *
     * @return
     */
    public TripleConstraint toConstraint() {
        Triple pattern = TripleUtils.create(getSource(), triplePattern.getPredicate(), getTarget(), isForward());
        TripleConstraint result = TripleConstraintImpl.create(pattern, exprs == null ? null : ExprUtils.andifyBalanced(exprs));
        return result;
    }

    /** A convenience shorthand for toConstraint().test(triple) */
    public boolean matches(Triple triple) {
        TripleConstraint c = toConstraint();
        boolean result = c.test(triple);
        return result;
//        Triple pattern = TripleUtils.create(getSource(), getTarget(), getSource(), isForward());
//        Binding binding = TripleUtils.tripleToBinding(pattern, triple);
//        boolean result = ExprListUtils.evalEffectiveBoolean(exprs, binding);
//        return result;
    }
}

