package org.aksw.jena_sparql_api.core.utils;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.ResultSetCloseable;

import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;


public class ResultSetUtils {
    public static ResultSetCloseable tripleIteratorToResultSet(Iterator<Triple> tripleIt, Closeable closeable) {
        Iterator<Binding> bindingIt = Iterators.transform(tripleIt, F_TripleToBinding.fn);
        QueryIter queryIter = new QueryIterPlainWrapper(bindingIt);
        List<String> varNames = Arrays.asList("s", "p", "o");
        ResultSet baseRs = ResultSetFactory.create(queryIter, varNames);
        ResultSetCloseable result = new ResultSetCloseable(baseRs, closeable);
        return result;
    }

}
