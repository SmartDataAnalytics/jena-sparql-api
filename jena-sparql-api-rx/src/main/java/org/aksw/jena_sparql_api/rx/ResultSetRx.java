package org.aksw.jena_sparql_api.rx;

import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.utils.query_execution.QueryExecutionAdapter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Template;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

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

    default QueryIterator asQueryIterator() {
        Flowable<Binding> bindingFlow = getBindings();
        return QueryIteratorUtils.createFromFlowable(bindingFlow);
    }

    /**
     * Returns a QueryExecution with only support for execSelect, abort and close
     *
     * @return A query execution wrapping this result set
     */
    default QueryExecution asQueryExecution() {
        return asQueryExecution(null);
    }

    default QueryExecution asQueryExecution(Template template) {

        QueryExecution result = new QueryExecutionAdapter() {
            protected Disposable disposable = null;

            @Override
            public Iterator<Quad> execConstructQuads() {
                if (template == null) {
                    throw new IllegalStateException("No template set");
                }

                return null;
                //TemplateLib.subst(quad, b, bNodeMap)
            }

            @Override
            public ResultSet execSelect() {
                if (disposable != null) {
                    throw new IllegalStateException("execSelect has already been called");
                }

                List<Var> vars = getVars();
                Flowable<Binding> flowable = getBindings();
                Iterator<Binding> it = flowable.blockingIterable().iterator();
                disposable = (Disposable)it;
                ResultSet result = ResultSetUtils.create2(vars, it);
                return result;
            }

            @Override
            public void abort() {
                super.abort();
                close();
            }

            @Override
            public void close() {
                if (disposable != null) {
                    disposable.dispose();
                }
                super.close();
            }
        };

        return result;
    }
}
