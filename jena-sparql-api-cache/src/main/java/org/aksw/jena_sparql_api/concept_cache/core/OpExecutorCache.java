package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.iterator.QueryIterService;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;


//class CacheConfig {
//    protected ConceptMap conceptMap;
//
//
//}

public class OpExecutorCache
    extends OpExecutor
{
    protected Map<Node, QueryExecutionFactory> serviceToQef;


    protected OpExecutorCache(ExecutionContext execCxt, Map<Node, QueryExecutionFactory> serviceToQef) {
        super(execCxt);
        this.serviceToQef = serviceToQef;
    }

    @Override
    protected QueryIterator execute(OpService opService, QueryIterator input) {

        Node serviceNode = opService.getService();
        String serviceUri = serviceNode.getURI();

        QueryIterator result;
        if(serviceUri.startsWith("cache://")) {
            //SparqlCacheUtils.
            QueryExecutionFactory qef = serviceToQef.get(serviceUri);
            if(qef == null) {
                throw new RuntimeException("Could not find a query execution factory for " + serviceUri);
            }

            ElementService rootElt = opService.getServiceElement();
            // By convention, the subElement must be a sub query
            ElementSubQuery subQueryElt = (ElementSubQuery)rootElt.getElement();
            Query query = subQueryElt.getQuery();

            QueryExecution qe = qef.createQueryExecution(query);
            ResultSet rs = qe.execSelect();


//            ResultSetViewCache.cacheResultSet(physicalRs, indexVars, indexResultSetSizeThreshold, conceptMap, pqfp);
//
//            QueryExecution qe = qef.createQueryExecution(query);
//            ResultSet rs = qe.execSelect();
//            QueryIterator result = new QueryIteratorResultSet(rs);
//
//            //QueryExecutionFactory
//
//            System.out.println("here");

            result = new QueryIteratorResultSet(rs);

        } else {
            result = super.exec(opService, input);
        }

        return result;
    }


}
