package org.aksw.sparqlqc.analysis.dataset;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class PaginatorQueryTriple
    extends PaginatorQueryBase<Triple>
{

    public PaginatorQueryTriple(QueryExecutionFactory qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Iterator<Triple> obtainResultIterator(QueryExecution qe) {
        Iterator<Triple> result = qe.execConstructTriples();
        return result;
    }
}

