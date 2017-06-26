package org.aksw.jena_sparql_api.mapper;

import java.util.Iterator;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.GuavaFunctionWrapper;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Iterators;


public class BindingMapperUtils {

    public static <T> Iterator<T> execMapped(QueryExecutionFactory qef, Query query, BindingMapper<T> bindingMapper) {
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();

        Iterator<Binding> itBinding = new IteratorResultSetBinding(rs);
        Function<Binding, T> fn = FunctionBindingMapper.create(bindingMapper);

        Iterator<T> result = Iterators.transform(itBinding, new GuavaFunctionWrapper<>(fn));
        return result;
    }

}
