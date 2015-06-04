package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.dirty.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConceptMap;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.Var;


public class QueryExecutionFactoryConceptCache
    extends QueryExecutionFactoryDecorator
{
    private ConceptMap conceptMap;

    public QueryExecutionFactoryConceptCache(QueryExecutionFactory decoratee) {
        super(decoratee);

        this.conceptMap = new ConceptMap();
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryExecution result = createQueryExecution(query);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {

        QueryExecution result;


        QuadFilterPattern qfp = ConceptMap.transform(query);

        CacheResult cacheResult;

        if(qfp == null) {
            cacheResult = null;
            //cacheHits = Collections.emptyList();
        } else {
            cacheResult = conceptMap.lookup(qfp);
        }


        //System.out.println("CacheHits: " + cacheHits.size());

        boolean isPatternFree = false;

        // Whether the query was exactly the same entry in the cache, so
        // that there is no need cache again
        boolean queryEqualedCache = false;

        if(cacheResult != null) {
            //CacheHit cacheHit = cacheHits.iterator().next();
            //CacheResult cacheResult = cacheHits;
            QuadFilterPatternCanonical qfpc = cacheResult.getReplacementPattern();

            queryEqualedCache = qfpc.isEmpty();
            //QuadFilterPatternCanonical remainder = qfpc.diff(queryQfpc);
            //queryEqualedCache = remainder.isEmpty();

            Op op = qfpc.toOp();

            Collection<Table> tables = cacheResult.getTables();
            Op opTable = null;
            for(Table table : tables) {
                Op tmp = OpTable.create(table);

                if(opTable == null) {
                    opTable = tmp;
                } else {
                    opTable = OpJoin.create(opTable, tmp);
                }
            }


            //System.out.println("Table size: " + table.size());

            if(op instanceof OpNull) {
                op = opTable;
            } else {
                op = OpJoin.create(opTable, op);
            }

            //System.out.println("Op: " + op.toString().substring(0, Math.min(2000, op.toString().length())));

            isPatternFree = OpUtils.isPatternFree(op);
            //System.out.println("isPatternFree: " + isPatternFree);

            Query yay = OpAsQuery.asQuery(op);



            yay.setQueryResultStar(false);
            yay.getProjectVars().clear();
            for(Var x : query.getProjectVars()) {
                yay.getProject().add(x);
            }

            //TODO We need to reset the projection...
            query = yay;
        }
        System.out.println("Running query: " + query.toString().substring(0, Math.min(2000, query.toString().length())));

        //System.out.println("Running query: " + query);


        boolean isIndexable = qfp != null;
        List<Var> vars = query.getProjectVars();

        if(isPatternFree) {
            QueryExecutionFactory ss = new QueryExecutionFactoryModel();
            result = ss.createQueryExecution(query);
        }
        else {
            QueryExecution qe = decoratee.createQueryExecution(query);

            if(isIndexable && !vars.isEmpty() && !queryEqualedCache) {
                Set<Var> indexVars = Collections.singleton(vars.iterator().next());

                result = new QueryExecutionConceptCache(qe, conceptMap, query, indexVars);
            } else {
                result = qe;
            }
        }

        // Check if the query is subject to indexing

        // Check if the query is subject to cache-injection

        return result;
    }

}