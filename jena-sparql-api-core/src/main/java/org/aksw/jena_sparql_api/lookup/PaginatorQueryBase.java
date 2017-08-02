package org.aksw.jena_sparql_api.lookup;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Range;
import com.google.common.collect.Streams;

public abstract class PaginatorQueryBase<T>
    implements ListPaginator<T>
{
    protected QueryExecutionFactory qef;
    protected Query query;

    protected abstract Iterator<T> obtainResultIterator(QueryExecution qe);


    public PaginatorQueryBase(QueryExecutionFactory qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

    public <X> ListPaginator<X> map(Function<Binding, X> fn) {
        //ListPaginator<X>
        return null;
    }

    @Override
    public Stream<T> apply(Range<Long> range) {
        Query clonedQuery = query.cloneQuery();
        range = Range.atLeast(0l).intersection(range);
        QueryUtils.applyRange(clonedQuery, range);

        QueryExecution qe = qef.createQueryExecution(clonedQuery);
        Iterator<T> it = obtainResultIterator(qe); // new IteratorResultSetBinding(qe.execSelect());

        Stream<T> result = Streams.stream(it);
        result.onClose(() -> qe.close());
        return result;
    }

    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountQuery(qef, query, itemLimit, rowLimit);
        return result;
    }
}
