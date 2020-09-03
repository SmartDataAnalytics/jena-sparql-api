package org.aksw.jena_sparql_api.rx;

import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;

/**
 * A result set based on rx Flowables.
 * Essentially a pair of variable (names) and a flowable of bindings.
 *
 * @author raven
 *
 */
public interface ResultSetRx {
    List<Var> getVars();
    Flowable<Binding> getBindings();

    // Return a fake QueryExecution with only execSelect() supported instead?
    // This would allow for closing the result set in a Jena-idiomatic way
//    default QueryExecution toQueryExecution() {
//    	new QueryExecutionA
//    }

    default ResultSet toResultSet() {
        List<Var> vars = getVars();
        Flowable<Binding> flowable = getBindings();
        Iterator<Binding> it = flowable.blockingIterable().iterator();

        // TODO Make closable!
        ResultSet result = ResultSetUtils.create2(vars, it);
        return result;
    }
}
