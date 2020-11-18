package org.aksw.jena_sparql_api.lookup;

import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.rxjava3.core.Flowable;

public class PaginatorQueryTriple
    extends PaginatorQueryBase<Triple>
{

    public PaginatorQueryTriple(SparqlQueryConnection qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Flowable<Triple> obtainResultIterator(Callable<QueryExecution> qeSupplier) {
        Flowable<Triple> result = SparqlRx.execConstructTriples(qeSupplier);
        //Iterator<Triple> result = qe.execConstructTriples();
        return result;
    }
}

