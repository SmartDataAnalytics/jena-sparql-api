package org.aksw.jena_sparql_api.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


public class SparqlPagingItemReader<T>
//    extends AbstractPagingItemReader<T>
    extends AbstractPaginatedDataItemReader<T>
{
    private QueryExecutionFactory sparqlService;
    private BindingMapper<T> bindingMapper;

    //private String queryString;
//    private String serviceUri;
//    private Collection<String> defaultGraphUris;
    
    //private volatile PagingQuery pagingQuery;
    private volatile Query query = null;
    
    private volatile Iterator<Query> itQuery = null; 

    
    public SparqlPagingItemReader() {
        setName(this.getClass().getName());
    }
    
    
//    @Override
//    public void ge
    
    
    public void setSparqlService(QueryExecutionFactory sparqlService) {
        this.sparqlService = sparqlService;
    }
    
    public QueryExecutionFactory getSparqlService() {
        return this.sparqlService;    
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

    
    
    /*
    public void setQueryString(String queryString) {
        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

        this.query = query;
        //this.queryString = queryString;
    }
    */

//    public String getServiceUri() {
//        return serviceUri;
//    }
//
//    public void setServiceUri(String serviceUri) {
//        this.serviceUri = serviceUri;
//    }
//
//    public Collection<String> getDefaultGraphUris() {
//        return defaultGraphUris;
//    }
//
//    public void setDefaultGraphUris(Collection<String> defaultGraphUris) {
//        this.defaultGraphUris = defaultGraphUris;
//    }

    public void setBindingMapper(BindingMapper<T> bindingMapper) {
        this.bindingMapper = bindingMapper;
    }

    public BindingMapper<?> getBindingMapper() {
        return this.bindingMapper;
    }

    @Override
    protected Iterator<T> doPageRead() {
        /*
        if (results == null) {
            results = new CopyOnWriteArrayList<T>();
        }
        else {
            results.clear();
        }
        */
        
        //if(itQuery == null) {
            PagingQuery pagingQuery = new PagingQuery(this.pageSize, this.query);
            Iterator<Query> itQuery = pagingQuery.createQueryIterator(this.page * this.pageSize);
        //}
        
        Query query = itQuery.next();
        
        if(query == null) {
            Collection<T> tmp = Collections.emptyList();
            return tmp.iterator();
        }
        
        //Query query = queryIterator.next();
        QueryExecution qe = sparqlService.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        

        List<T> items = new ArrayList<T>();
        long rowId = 0;
        while(rs.hasNext()) {
            ++rowId;
            Binding binding = rs.nextBinding();
            
            T item = bindingMapper.map(binding, rowId);
            
            items.add(item);
            //results.add(item);
        }
        
        return items.iterator();        
    }
    
//    @Override
//    protected void doReadPage() {
//        if (results == null) {
//            results = new CopyOnWriteArrayList<T>();
//        }
//        else {
//            results.clear();
//        }
//        
//        if(itQuery == null) {
//            doJumpToPage(0);
//        }
//        
//        Query query = itQuery.next();
//        
//        if(query == null) {
//            return;
//        }
//        
//        //Query query = queryIterator.next();
//        QueryExecution qe = sparqlService.createQueryExecution(query);
//        ResultSet rs = qe.execSelect();
//        
//
//        long rowId = 0;
//        while(rs.hasNext()) {
//            ++rowId;
//            Binding binding = rs.nextBinding();
//            
//            T item = bindingMapper.map(binding, rowId);
//            
//            results.add(item);
//        }
//        
//    }


    /*
    @Override
    protected void doJumpToPage(int itemIndex) {
        PagingQuery pagingQuery = new PagingQuery(getPageSize(), query);
        Iterator<Query> itQuery = pagingQuery.createQueryIterator(itemIndex);
        
        this.itQuery = itQuery;
    }
    */

}
