package org.aksw.jena_sparql_api.lookup;

import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;

public class PaginatorQueryBinding
    extends PaginatorQueryBase<Binding>
{

    public PaginatorQueryBinding(SparqlQueryConnection qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Flowable<Binding> obtainResultIterator(Callable<QueryExecution> qeSupplier) {
        Flowable<Binding> result = SparqlRx.execSelectRaw(qeSupplier);

//        ResultSet rs = qe.execSelect();
//        Iterator<Binding> result = new IteratorResultSetBinding(rs);
        return result;
    }
}
