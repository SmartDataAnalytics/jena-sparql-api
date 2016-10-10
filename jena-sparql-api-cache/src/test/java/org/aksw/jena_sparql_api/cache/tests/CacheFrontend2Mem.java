package org.aksw.jena_sparql_api.cache.tests;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;



class QueryExecutionSliceSupplier
    implements Function<Range<Long>, QueryExecution>
{
    protected QueryExecutionFactory qef;
    protected Query queryTemplate;
    
    @Override
    public QueryExecution apply(Range<Long> range) {
        Query query = queryTemplate.cloneQuery();
        QueryUtils.applyRange(query, range);

        QueryExecution result = qef.createQueryExecution(query);
        
        return result;
    }
}


class QueryExecutionSegmentedCache {
    protected Query query;    
    
    protected Map<Var, Var> varMap; // Mapping to rename variables of the bindings in the cache
    //protected SegmentedList<Binding> listCache;
    protected Function<Range<Long>, ClosableIterator<Binding>> bindingSupplier;

    protected Range<Long> range;
    protected List<String> varNames;
    
    protected transient ClosableIterator<Binding> it;
    
    public ResultSet execSelect() {
        it = bindingSupplier.apply(range);
        
        Iterator<Binding> i = it;
        if(varMap != null) {
            Iterable<Binding> tmp = () -> it;
            i = StreamSupport.stream(tmp.spliterator(), false)
                .map(b -> BindingUtils.rename(b, varMap))
                .iterator();
        }        
        
        ResultSet result = ResultSetUtils.create(varNames, i);
        return result;
        
    }
    
    public void close() {
        if(it != null) {
            it.close();
        }
    }
    
    
    
    public static void main(String[] args) {
        QueryExecutionFactory qef = FluentQueryExecutionFactory
            .from(ModelFactory.createDefaultModel())
            .create();
        
        
    }
}




