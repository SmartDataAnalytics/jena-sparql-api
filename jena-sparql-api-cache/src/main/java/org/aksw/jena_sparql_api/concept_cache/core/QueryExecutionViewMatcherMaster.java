package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.views.index.LookupResult;
import org.aksw.jena_sparql_api.views.index.OpViewMatcher;
import org.aksw.jena_sparql_api.views.index.OpViewMatcherImpl;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
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


    public ResultSetCloseable createResultSet(RangedSupplier<Long, Binding> rangedSupplier, Range<Long> range, Map<Var, Var> varMap) {
    	ClosableIterator<Binding> it = rangedSupplier.apply(range);
    	Iterable<Binding> tmp = () -> it;
    	Stream<Binding> stream = StreamSupport.stream(tmp.spliterator(), false);
    	if(varMap != null) {
    		stream = stream.map(b -> BindingUtils.rename(b, varMap));
    	}
    	stream = stream.onClose(() -> it.close());

    	List<String> varNames = query.getResultVars();
    	//ResultSetCloseable result = ResultSetUtils.create(varNames, it);
    	ResultSet rs = ResultSetUtils.create(varNames, stream.iterator());
    	ResultSetCloseable result = new ResultSetCloseable(rs);

    	return result;
    }

    @Override
    protected ResultSetCloseable executeCoreSelect(Query query) {

    	Range<Long> range = QueryUtils.toRange(query);
    	Query q = query.cloneQuery();
    	q.setLimit(Query.NOLIMIT);
    	q.setOffset(Query.NOLIMIT);

    	Op opCache = Algebra.toQuadForm(Algebra.compile(q));

        OpViewMatcher viewMatcher = OpViewMatcherImpl.create();
        Node id = viewMatcher.add(opCache);
        LookupResult lr = viewMatcher.lookupSingle(opCache);
        RangedSupplier<Long, Binding> rangedSupplier;
        Map<Var, Var> varMap;
        if(lr == null) {
        	// Obtain the supplier from a factory (the factory may e.g. manage the sharing of a thread pool)
        	rangedSupplier = new RangedSupplierQuery(parentFactory, q);
        	opToRangedSupplier.put(id, rangedSupplier);
        	varMap = null;
        }
        else {

            varMap = Iterables.getFirst(lr.getOpVarMap().getVarMaps(), null);

        	Node entryId = lr.getEntry().id;
        	rangedSupplier = opToRangedSupplier.get(entryId);
        }

        ResultSetCloseable result = createResultSet(rangedSupplier, range, varMap);
        return result;
    }


	@Override
	protected QueryExecution executeCoreSelectX(Query query) {
		// TODO Fix bad design - this method is not needed
		return null;
	}

}

