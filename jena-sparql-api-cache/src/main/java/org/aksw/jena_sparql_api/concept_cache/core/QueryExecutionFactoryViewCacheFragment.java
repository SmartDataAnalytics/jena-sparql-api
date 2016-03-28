package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.dirty.ConceptMap;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Var;

/**
 * QueryExecutionFactory for executing and caching fragments of a query.
 *
 * Do not use this class directly, instead use QueryExecutionFactoryViewCacheMaster.
 *
 * @author raven
 *
 */
public class QueryExecutionFactoryViewCacheFragment
    extends QueryExecutionFactoryDecorator
{
    protected ConceptMap conceptMap;
    protected long indexResultSetSizeThreshold;

    public QueryExecutionFactoryViewCacheFragment(QueryExecutionFactory decoratee, ConceptMap conceptMap, long indexResultSetSizeThreshold) {
        super(decoratee);
        this.conceptMap = conceptMap;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
    }


    @Override
    public QueryExecution createQueryExecution(Query query) {
        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(query);
        if(pqfp == null) {
            throw new RuntimeException("Query is not indexable: " + query);
        }

        Set<Var> indexVars = new HashSet<>(query.getProjectVars());

        QueryExecution result = new QueryExecutionViewCacheFragment(query, pqfp, decoratee, conceptMap, indexVars, indexResultSetSizeThreshold);
        return result;
    }

}
