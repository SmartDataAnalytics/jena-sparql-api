package org.aksw.jena_sparql_api.lookup;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import io.reactivex.Flowable;

public class PaginatorQueryTriple
    extends PaginatorQueryBase<Triple>
{

    public PaginatorQueryTriple(QueryExecutionFactory qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Flowable<Triple> obtainResultIterator(Supplier<QueryExecution> qeSupplier) {
    	Flowable<Triple> result = ReactiveSparqlUtils.execConstructTriples(qeSupplier);
        //Iterator<Triple> result = qe.execConstructTriples();
        return result;
    }
}

