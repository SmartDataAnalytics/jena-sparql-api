package org.aksw.jena_sparql_api.batch;

import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionBase;
import com.hp.hpl.jena.sparql.function.FunctionFactory;


public class FunctionFactoryCache
    implements FunctionFactory
{
    private FunctionFactory delegate;
    private Cache<List<NodeValue>, NodeValue> cache;

    public FunctionFactoryCache(FunctionFactory delegate, Cache<List<NodeValue>, NodeValue> cache) {
        super();
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public Function create(String name) {
        Function tmp = delegate.create(name);
        FunctionBase x = (FunctionBase)tmp;
        Function result = new FunctionCache(x, cache);
        return result;
    }


    public static FunctionFactoryCache create(FunctionFactory delegate) {
        Cache<List<NodeValue>, NodeValue> cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        FunctionFactoryCache result = create(delegate, cache);
        return result;
    }

    public static FunctionFactoryCache create(FunctionFactory delegate, Cache<List<NodeValue>, NodeValue> cache) {
        FunctionFactoryCache result = new FunctionFactoryCache(delegate, cache);
        return result;
    }

}
