package org.aksw.jena_sparql_api.lookup;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;

public class PaginatorQueryBinding
    extends PaginatorQueryBase<Binding>
{

    public PaginatorQueryBinding(QueryExecutionFactory qef, Query query) {
        super(qef, query);
    }

    @Override
    protected Iterator<Binding> obtainResultIterator(QueryExecution qe) {
        ResultSet rs = qe.execSelect();
        Iterator<Binding> result = new IteratorResultSetBinding(rs);
        return result;
    }
}
