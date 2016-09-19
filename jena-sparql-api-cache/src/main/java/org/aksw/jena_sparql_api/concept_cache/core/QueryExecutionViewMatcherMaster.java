package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.views.index.LookupResult;
import org.aksw.jena_sparql_api.views.index.OpViewMatcher;
import org.aksw.jena_sparql_api.views.index.OpViewMatcherImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

public class QueryExecutionViewMatcherMaster
	extends QueryExecutionBaseSelect
{
	protected OpViewMatcher viewMatcher;
	protected Map<Node, RangedSupplier<Long, Binding>> opToRangedSupplier;

    protected long indexResultSetSizeThreshold;

    //protected Map<Node, ? super ViewCacheIndexer> serviceMap;



    public QueryExecutionViewMatcherMaster(
    		Query query,
    		QueryExecutionFactory subFactory,
    		OpViewMatcher viewMatcher,
    		Map<Node, RangedSupplier<Long, Binding>> opToRangedSupplier
    		//long indexResultSetSizeThreshold,
    		//Map<Node, ? super ViewCacheIndexer> serviceMap
    ) {
    	super(query, subFactory);

    	this.viewMatcher = viewMatcher;
    	this.opToRangedSupplier = opToRangedSupplier;
    	//this.serviceMap = serviceMap;
    }


    public ResultSetCloseable createResultSet(RangedSupplier<Long, Binding> rangedSupplier, Range<Long> range) {
    	ClosableIterator<Binding> it = rangedSupplier.apply(range);
    	List<String> varNames = query.getResultVars();
    	//ResultSetCloseable result = ResultSetUtils.create(varNames, it);
    	ResultSet rs = ResultSetUtils.create(varNames, it);
    	ResultSetCloseable result = new ResultSetCloseable(rs);

    	return result;
    }

    @Override
    protected ResultSetCloseable executeCoreSelect(Query query) {
    	Range<Long> range = QueryUtils.toRange(query);


    	Op opCache = Algebra.toQuadForm(Algebra.compile(query));

        OpViewMatcher viewMatcher = OpViewMatcherImpl.create();
        Node id = viewMatcher.add(opCache);
        LookupResult lr = viewMatcher.lookupSingle(opCache);


        RangedSupplier<Long, Binding> rangedSupplier;

        if(lr == null) {
        	// Obtain the supplier from a factory (the factory may e.g. manage the sharing of a thread pool)
        	rangedSupplier = new RangedSupplierQuery(parentFactory, query);
        	opToRangedSupplier.put(id, rangedSupplier);
        }
        else {
        	Node entryId = lr.getEntry().id;
        	rangedSupplier = opToRangedSupplier.get(entryId);
        }

        ResultSetCloseable result = createResultSet(rangedSupplier, range);
        return result;
    }


	@Override
	protected QueryExecution executeCoreSelectX(Query query) {
		// TODO Fix bad design - this method is not needed
		return null;
	}

}

