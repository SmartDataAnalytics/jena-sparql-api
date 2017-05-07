package org.aksw.sparqlqc.analysis.dataset;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryGenerationUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Range;

public class PaginatorQuery
    implements ListPaginator<Binding>
{
    protected QueryExecutionFactory qef;
    protected Query query;

    public PaginatorQuery(QueryExecutionFactory qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

    public <X> ListPaginator<X> map(Function<Binding, X> fn) {
        //ListPaginator<X>
        return null;
    }

    @Override
    public Stream<Binding> apply(Range<Long> range) {
        Query clonedQuery = query.cloneQuery();
        range = Range.atLeast(0l).intersection(range);
        QueryUtils.applyRange(clonedQuery, range);

        QueryExecution qe = qef.createQueryExecution(clonedQuery);
        Iterator<Binding> it = new IteratorResultSetBinding(qe.execSelect());

        Stream<Binding> result = StreamUtils.stream(it);
        result.onClose(() -> qe.close());
        return result;
    }

    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountQuery(qef, query, itemLimit, rowLimit);
        return result;
    }

}
