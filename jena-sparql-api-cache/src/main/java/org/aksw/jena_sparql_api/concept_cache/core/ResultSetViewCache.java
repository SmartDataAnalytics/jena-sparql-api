package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Iterators;

class CollectionCacheIterator<T>
    implements Iterator<T>
{

    @Override
    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public T next() {
        // TODO Auto-generated method stub
        return null;
    }


}

class CollectionCache<T> {
    protected List<T> items;
    protected boolean isComplete;

}

class AsyncCollection<T> {
    protected List<T> items;
    protected boolean isComplete;
}


public class ResultSetViewCache {

    public static Entry<ResultSet, Boolean> cacheResultSetParallel(ResultSet physicalRs, Set<Var> indexVars)
    {
        //ArrayBlockingQueue<Binding> deque = new ArrayBlockingQueue<>(100);
        return null;
    }

    /**
     * Reads the first threshold bindings from the given result set, and attempts to cache them,
     * unless the result set turns out to be too large.
     *
     * Returns a new result together with a flag of whether (true) or not (false) caching was performed.
     *
     *
     * @param physicalRs
     * @param indexVars
     * @param indexResultSetSizeThreshold
     * @param sparqlViewCache
     * @param pqfp
     * @return
     */
    public static Entry<ResultSet, Boolean> cacheResultSet(ResultSet physicalRs, Set<Var> indexVars, long indexResultSetSizeThreshold, SparqlViewMatcherQfpc sparqlViewCache, ProjectedQuadFilterPattern pqfp) {

        ResultSet resultRs;
        //ResultSet physicalRs = decoratee.execSelect();
        List<String> varNames = physicalRs.getResultVars();

        List<Binding> bindings = new ArrayList<Binding>();

        // Start collecting bindings from the result set until we reach the threshold
        int i;
        for(i = 0; i < indexResultSetSizeThreshold && physicalRs.hasNext(); ++i) {
            Binding binding = physicalRs.nextBinding();
            bindings.add(binding);
        }

        boolean isCacheable = i <= indexResultSetSizeThreshold;

        if(isCacheable) {
            ResultSetPart tmp = new ResultSetPart(varNames, bindings);


            //it = bindings.iterator();
            //ResultSet tmp = new ResultSetStream(varNames, null, bindings.iterator());

            //resultRs = ResultSetFactory.copyResults(tmp);

            QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
            QuadFilterPatternCanonical qfpc = AlgebraUtils.canonicalize2(qfp, VarGeneratorImpl2.create("v"));

            //ResultSet cacheRs = ResultSetUtils.project(resultRs, indexVars, true);
            Table table = ResultSetPart.toTable(tmp);
            if(false) {
            	//sparqlViewCache.index(qfpc, table); //cacheRs);
            }

            resultRs = ResultSetPart.toResultSet(tmp);

        } else {
            // TODO Resource leak if the physicalRs is not consumed - fix that somehow!
            // TODO Change to stream api because these can be closed!
            Iterator<Binding> it = Iterators.concat(bindings.iterator(), new IteratorResultSetBinding(physicalRs));
            resultRs = new ResultSetStream(varNames, null, it);
        }

        Entry<ResultSet, Boolean> result = new SimpleEntry<>(resultRs, isCacheable);

        return result;
    }

}
