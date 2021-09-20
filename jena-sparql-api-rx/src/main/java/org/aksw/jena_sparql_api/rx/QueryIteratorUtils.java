package org.aksw.jena_sparql_api.rx;

import java.util.Iterator;

import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

public class QueryIteratorUtils {

    /**
     * Obtain a blocking iterable from the flow and wrap it as a QueryIterator.
     * Closing the latter cascades to the disaposable obtained from the flowable.
     */
    public static QueryIterator createFromFlowable(Flowable<Binding> bindingFlow) {
        Iterator<Binding> tmp = bindingFlow.blockingIterable().iterator();
        QueryIterator result = new QueryIterPlainWrapper(tmp) {
            @Override
            protected void requestCancel() {
                ((Disposable)tmp).dispose();
                super.requestCancel();
            }

            @Override
            protected void closeIterator() {
                ((Disposable)tmp).dispose();
                super.closeIterator();
            }
        };

        return result;
    }
}
