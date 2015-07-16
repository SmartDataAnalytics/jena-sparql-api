package org.aksw.jena_sparql_api.mapper;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


public class BindingMapperUtils {
    
    public static <T> Iterator<T> execMapped(QueryExecutionFactory qef, Query query, BindingMapper<T> bindingMapper) {
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
       
        Iterator<Binding> itBinding = new IteratorResultSetBinding(rs);       
        Function<Binding, T> fn = FunctionBindingMapper.create(bindingMapper);
       
        Iterator<T> result = Iterators.transform(itBinding, fn);
        return result;
    }
    
}
