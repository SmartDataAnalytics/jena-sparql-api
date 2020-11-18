package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.function.Predicate;

import org.aksw.jena_sparql_api.sparql_path2.NestedPath;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.jgrapht.Graph;

/**
 * Tests nested paths instances for whether the last two edges
 * are in accordance by the join summary
 *
 */
public class JoinSummaryPredicate<V, E>
    implements Predicate<NestedPath<V, E>>
{
    protected Graph<E, ?> joinSummary;
    //protected targetEdges;

    public JoinSummaryPredicate(Graph<E, ?> joinSummary) {
        this.joinSummary = joinSummary;
    }


    @Override
    public boolean test(NestedPath<V, E> t) {

        boolean result = t.getParentLink().map(plB ->
            plB.getTarget().getParentLink().map(plA -> {
                Directed<E> da = plA.getDiProperty();
                Directed<E> db = plB.getDiProperty();

                E a = da.getValue();
                E b = db.getValue();

                boolean r;
                if(da.isForward()) {
                    if(db.isForward()) {
                        r = joinSummary.containsEdge(a, b);
                    } else {
                        r = true;
                    }
                } else {
                    if(db.isForward()) {
                        r = true;
                    } else {
                        r = joinSummary.containsEdge(b, a);
                    }
                }
                return r;
            }).orElse(false)
        ).orElse(false);


        return result;
    }

}
