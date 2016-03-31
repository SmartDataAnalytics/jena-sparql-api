package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

/**
 * TODO This is not a usual qef, because it also needs an op with which the result set is associated
 *
 * QueryExecutionFactory for executing and caching fragments of a query.
 *
 * Do not use this class directly, instead use QueryExecutionFactoryViewCacheMaster.
 *
 * @author raven
 *
 */
public class QueryExecutionFactoryViewCacheFragment
//    extends QueryExecutionFactoryDecorator
{
    protected QueryExecutionFactory decoratee;
    protected SparqlViewCache conceptMap;
    protected long indexResultSetSizeThreshold;

    public QueryExecutionFactoryViewCacheFragment(QueryExecutionFactory decoratee, SparqlViewCache conceptMap, long indexResultSetSizeThreshold) {
        //super(decoratee);
        this.decoratee = decoratee;
        this.conceptMap = conceptMap;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
    }

    //@Override
    public QueryExecution createQueryExecution(Op indexPattern, Query query) {
        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(indexPattern);
        if(pqfp == null) {
            throw new RuntimeException("Query is not indexable: " + query);
        }

        Set<Var> indexVars = new HashSet<>(query.getProjectVars());

        QueryExecution result = new QueryExecutionViewCacheFragment(query, pqfp, decoratee, conceptMap, indexVars, indexResultSetSizeThreshold);
        return result;
    }

}
