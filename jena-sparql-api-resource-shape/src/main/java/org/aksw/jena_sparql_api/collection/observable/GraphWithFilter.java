package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.jena_sparql_api.relation.TripleConstraint;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

/**
 *
 *
 * @author raven
 *
 */
public class GraphWithFilter
    extends GraphBase
{
    protected Graph delegate;
    protected TripleConstraint predicate;

    public GraphWithFilter(Graph graph, TripleConstraint predicate) {
        // super(graph);
        this.delegate = graph;
        this.predicate = predicate;
    }

    public Graph get() {
        return delegate;
    }

    @Override
    public void performAdd(Triple t) /* throws AddDeniedException */ {
        boolean isAccepted = predicate.test(t);
        if (isAccepted) {
            get().add(t);
        }
    }

    @Override
    public void performDelete(Triple t) /* throws AddDeniedException */ {
        boolean isAccepted = predicate.test(t);
        if (isAccepted) {
            get().delete(t);
        }
    }

//    @Override
//    public void remove(Node s, Node p, Node o) {
//
//    	super.remove(s, p, o);
//    }

//    @Override
//    public boolean contains(Node s, Node p, Node o) {
//        boolean isAccepted = predicate.test(new Triple(s, p, o));
//        boolean result = isAccepted && super.contains(s, p, o);
//
//        return result;
//    }



    /** Combine the filter pattern with a user provided one. Returns null if not satisfiable */
    public Triple createCombinedPattern(Node s, Node p, Node o) {
        Triple a = predicate.getMatchTriple();
        Triple b = Triple.createMatch(s, p, o);
        Triple result = TripleUtils.logicalAnd(a, b);
        return result;
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        Triple combinedPattern = createCombinedPattern(s, p, o);

        get().remove(combinedPattern.getMatchSubject(),
                combinedPattern.getMatchPredicate(),
                combinedPattern.getMatchObject());
    }

//    @Override
//    public void clear() {
//        GraphUtil.remove(this, Node.ANY, Node.ANY, Node.ANY);
//    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple t) {
        Triple combinedPattern = createCombinedPattern(t.getMatchSubject(), t.getMatchPredicate(), t.getMatchObject());

        ExtendedIterator<Triple> result = combinedPattern == null
                ? NiceIterator.emptyIterator()
                : get().find(combinedPattern).filterKeep(predicate::test);

        return result;
    }
}
