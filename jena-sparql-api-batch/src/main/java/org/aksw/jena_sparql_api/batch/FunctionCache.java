package org.aksw.jena_sparql_api.batch;

import java.util.List;

import com.google.common.cache.Cache;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class FunctionCache
    extends FunctionBase
{
    private FunctionBase delegate;
    public Cache<List<NodeValue>, NodeValue> cache;


    public FunctionCache(FunctionBase delegate, Cache<List<NodeValue>, NodeValue> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public void checkBuild(String arg0, ExprList arg1) {
        delegate.checkBuild(arg0, arg1);
//        if(delegate instanceof FunctionBase) {
//            FunctionBase tmp = (FunctionBase)delegate;
//            tmp.checkBuild(arg0, arg1);
//        }
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        NodeValue result = cache.getIfPresent(args);
        if(result == null) {
            result = delegate.exec(args);
            cache.put(args, result);
        }
        return result;
    }

}
