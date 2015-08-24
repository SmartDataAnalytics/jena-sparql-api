package org.aksw.jena_sparql_api.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.pagination.core.PagingQuery;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


/**
 * Item reader that reads a SPARQL SELECT query using pagination
 *
 * @author raven
 *
 * @param <T>
 */
public class ItemReaderModel
//    extends AbstractPagingItemReader<T>
    extends AbstractPaginatedDataItemReader<Model>
{
    private QueryExecutionFactory qef;
    private volatile Query query = null;

    private volatile Iterator<Query> itQuery = null;


    public ItemReaderModel() {
        setName(this.getClass().getName());
    }

    public void setSparqlService(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    public QueryExecutionFactory getQueryExecutionFactory() {
        return this.qef;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getQueryString() {
        return "" + query;
    }

    @Override
    protected Iterator<Model> doPageRead() {
        /*
        if (results == null) {
            results = new CopyOnWriteArrayList<T>();
        }
        else {
            results.clear();
        }
        */

//        PagingQuery pagingQuery = new PagingQuery(this.pageSize, this.query);
//        Iterator<Query> itQuery = pagingQuery.createQueryIterator(this.page * this.pageSize);
//
//        Query query = itQuery.next();
//
//        if(query == null) {
//            Collection<T> tmp = Collections.emptyList();
//            return tmp.iterator();
//        }
//
//        //Query query = queryIterator.next();
//        QueryExecution qe = qef.createQueryExecution(query);
//        ResultSet rs = qe.execSelect();
//
//
//        List<T> items = new ArrayList<T>();
//        long rowId = 0;
//        while(rs.hasNext()) {
//            ++rowId;
//            Binding binding = rs.nextBinding();
//
//            T item = bindingMapper.map(binding, rowId);
//
//            items.add(item);
//            //results.add(item);
//        }
//
//        return items.iterator();
        return null;
    }
}
