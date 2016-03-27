package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.dirty.ConceptMap;
import org.aksw.jena_sparql_api.core.QueryExecutionAdapter;
import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionDecoratorBase;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionSelect;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutionViewCachePartial
    extends QueryExecutionAdapter
//    extends QueryExect
//extends QueryExecutionSelect
{

    private static final Logger logger = LoggerFactory
            .getLogger(QueryExecutionViewCachePartial.class);

    protected QueryExecutionFactory qef;
    private Query query;
    private ConceptMap conceptMap;
    private Set<Var> indexVars;
    private long indexResultSetSizeThreshold;

    public QueryExecutionViewCachePartial(Query query, QueryExecutionFactory qef, ConceptMap conceptMap, Set<Var> indexVars, long indexResultSetSizeThreshold) {
        this.query = query;
        this.qef = qef;
        this.conceptMap = conceptMap;
        this.indexVars = indexVars;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
    }

    @Override
    public ResultSet execSelect() {
        QueryExecution qe = SparqlCacheUtils.prepareQueryExecution(qef, query, conceptMap, indexResultSetSizeThreshold);
        ResultSet result = qe.execSelect();

        return result;
    }



    //@Override
//    protected QueryExecution executeCoreSelectX(Query query) {
//        QueryExecution qe = SparqlCacheUtils.prepareQueryExecution(qef, query, conceptMap, indexResultSetSizeThreshold);
//        return qe;
//    }
}

//
//public static boolean canCacheQuery(Query query, long rsSize) {
//    long limit = query.getLimit();
//    long offset = query.getOffset();
//
//    boolean isZeroOffset = offset == Query.NOLIMIT || offset == 0;
//    boolean isCompleteResult = limit == Query.NOLIMIT || rsSize < limit;
//
//    boolean result = isZeroOffset && isCompleteResult;
//
//    return result;
//}
//
//public ResultSet tryToCacheResultSet(ResultSet rs) {
//    ResultSetRewindable result = ResultSetFactory.copyResults(rs);
//    long rsSize = result.size();
//
//    boolean canIndex = canCacheQuery(query, rsSize);
//
//    if(canIndex) {
//        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(query);
//
//        if(pqfp != null) {
//            QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
//
//            ResultSet cacheRs = ResultSetUtils.project(result, indexVars, true);
//
//            conceptMap.index(qfp, cacheRs);
//        } else {
//            logger.warn("Could not index: " + query);
//        }
//    }
//
//    return result;
//}

//
//
//ResultSet result;
//ResultSet physicalRs = decoratee.execSelect();
//List<String> varNames = physicalRs.getResultVars();
//
//List<Binding> bindings = new ArrayList<Binding>();
//
//int i;
//for(i = 0; i < indexResultSetSizeThreshold && physicalRs.hasNext(); ++i) {
//    Binding binding = physicalRs.nextBinding();
//    bindings.add(binding);
//}
//
//boolean exceededThreshold = i >= indexResultSetSizeThreshold;
//
//if(exceededThreshold) {
//    // TODO Resource leak if the physicalRs is not consumed - fix that somehow!
//    Iterator<Binding> it = Iterators.concat(bindings.iterator(), new IteratorResultSetBinding(physicalRs));
//    result = new ResultSetStream(varNames, null, it);
//} else {
//    //it = bindings.iterator();
//    ResultSet tmp = new ResultSetStream(varNames, null, bindings.iterator());
//
//    result = tryToCacheResultSet(tmp);
//}
//
//return result;
