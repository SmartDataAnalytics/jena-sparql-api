package org.aksw.jena_sparql_api.concept_cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionDecoratorBase;

import com.google.common.collect.Iterators;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class QueryExecutionConceptCache
    extends QueryExecutionDecoratorBase<QueryExecution>
{
    private ConceptMap conceptMap;
    private Query query;
    private Set<Var> indexVars;
    private long indexResultSetSizeThreshold = 20000;

    public QueryExecutionConceptCache(QueryExecution decoratee, ConceptMap conceptMap, Query query, Set<Var> indexVars) {
        super(decoratee);

        this.conceptMap = conceptMap;
        this.query = query;
        this.indexVars = indexVars;
    }

    public static boolean canIndexQuery(Query query, long rsSize) {
        long limit = query.getLimit();
        long offset = query.getOffset();

        boolean isZeroOffset = offset == Query.NOLIMIT || offset == 0;
        boolean isCompleteResult = limit == Query.NOLIMIT || rsSize < limit;

        boolean result = isZeroOffset && isCompleteResult;

        return result;
    }

    public ResultSet tryIndex(ResultSet rs) {
        ResultSetRewindable result = ResultSetFactory.copyResults(rs);
        long rsSize = result.size();

        boolean canIndex = canIndexQuery(query, rsSize);

        if(canIndex) {
            ResultSet cacheRs = ResultSetUtils.project(result, indexVars, true);
            conceptMap.index(query, cacheRs);
        }

        return result;
    }

    @Override
    public ResultSet execSelect() {

        ResultSet result;
        ResultSet physicalRs = decoratee.execSelect();
        List<String> varNames = physicalRs.getResultVars();

        List<Binding> bindings = new ArrayList<Binding>();

        int i;
        for(i = 0; i < indexResultSetSizeThreshold && physicalRs.hasNext(); ++i) {
            Binding binding = physicalRs.nextBinding();
            bindings.add(binding);
        }

        boolean exceededThreshold = i >= indexResultSetSizeThreshold;

        if(exceededThreshold) {
            // TODO Resource leak if the physicalRs is not consumed - fix that somehow!
            Iterator<Binding> it = Iterators.concat(bindings.iterator(), new IteratorResultSetBinding(physicalRs));
            result = new ResultSetStream(varNames, null, it);
        } else {
            //it = bindings.iterator();
            ResultSet tmp = new ResultSetStream(varNames, null, bindings.iterator());

            result = tryIndex(tmp);
        }

        return result;
    }
}

