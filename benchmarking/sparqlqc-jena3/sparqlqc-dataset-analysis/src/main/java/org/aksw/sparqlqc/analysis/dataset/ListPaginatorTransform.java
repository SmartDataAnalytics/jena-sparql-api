package org.aksw.sparqlqc.analysis.dataset;

import java.util.stream.Stream;

import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListPaginator;

import com.google.common.collect.Range;

public class ListPaginatorTransform<T>
    implements ListPaginator<T>
{
    protected ListPaginator<T> fn;

    //public ListPaginatorTransform<T>

    @Override
    public Stream<T> apply(Range<Long> t) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        // TODO Auto-generated method stub
        return null;
    }

}
