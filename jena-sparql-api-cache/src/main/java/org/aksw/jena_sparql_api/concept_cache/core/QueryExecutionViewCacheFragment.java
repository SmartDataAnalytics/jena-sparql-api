package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.query_execution.QueryExecutionAdapter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A query execution wrapper that performs caching of a query.
 * Hence, upon calling exec* (TODO derive from a base class that maps all requests to select queries),
 * the specified query is executed with the given query execution factory,
 * and the result set is associated with a configured key (the pqfp) and added to an index.
 *
 *
 * @author raven
 *
 */
public class QueryExecutionViewCacheFragment
    extends QueryExecutionAdapter
{
    protected QueryExecutionFactory qef;
    protected ProjectedQuadFilterPattern pqfp;
    private Query query;
    private SparqlViewMatcherQfpc conceptMap;
    private Set<Var> indexVars;
    private long indexResultSetSizeThreshold;

    public QueryExecutionViewCacheFragment(Query query, ProjectedQuadFilterPattern pqfp, QueryExecutionFactory qef, SparqlViewMatcherQfpc conceptMap, Set<Var> indexVars, long indexResultSetSizeThreshold) {
        this.query = query;
        this.pqfp = pqfp;
        this.qef = qef;
        this.conceptMap = conceptMap;
        this.indexVars = indexVars;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
    }

    @Override
    public ResultSet execSelect() {
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet physicalRs = qe.execSelect();
        Entry<ResultSet, Boolean> entry = ResultSetViewCache.cacheResultSet(physicalRs, indexVars, indexResultSetSizeThreshold, conceptMap, pqfp);
        ResultSet result = entry.getKey();
        return result;
    }
}

