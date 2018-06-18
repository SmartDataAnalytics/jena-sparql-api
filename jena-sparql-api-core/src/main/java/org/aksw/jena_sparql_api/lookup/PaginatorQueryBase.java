package org.aksw.jena_sparql_api.lookup;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import com.google.common.collect.Range;

import io.reactivex.Flowable;
import io.reactivex.Single;

public abstract class PaginatorQueryBase<T>
    implements ListPaginator<T>
{
    protected QueryExecutionFactory qef;
    protected Query query;

    protected abstract Flowable<T> obtainResultIterator(Supplier<QueryExecution> qe);


    public PaginatorQueryBase(QueryExecutionFactory qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

//    public <X> ListPaginator<X> map(Function<Binding, X> fn) {
//        //ListPaginator<X>
//        return null;
//    }

    @Override
    public Flowable<T> apply(Range<Long> range) {
        Query clonedQuery = query.cloneQuery();
        range = Range.atLeast(0l).intersection(range);
        QueryUtils.applyRange(clonedQuery, range);


        Flowable<T> result = obtainResultIterator(() -> qef.createQueryExecution(clonedQuery)); // new IteratorResultSetBinding(qe.execSelect());

//        Stream<T> result = Streams.stream(it);
//        result.onClose(() -> qe.close());
        return result;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
    	Single<Range<Long>> result = ReactiveSparqlUtils.fetchCountQuery(qef, query, itemLimit, rowLimit);
        //Range<Long> result = ServiceUtils.fetchCountQuery(qef, query, itemLimit, rowLimit);
        return result;
    }
}
