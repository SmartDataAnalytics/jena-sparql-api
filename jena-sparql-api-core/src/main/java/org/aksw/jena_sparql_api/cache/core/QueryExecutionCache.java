package org.aksw.jena_sparql_api.cache.core;

import java.io.IOException;

import org.aksw.jena_sparql_api.cache.extra.Cache;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.aksw.jena_sparql_api.core.QueryExecutionStreamingDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:11 PM
 */
public class QueryExecutionCache
    extends QueryExecutionStreamingDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCache.class);


    private Cache cache;
    private String queryString;

    public QueryExecutionCache(QueryExecutionStreaming decoratee, String queryString, Cache cache) {
        super(decoratee);

        this.queryString = queryString;
        this.cache = cache;
    }


    public ResultSet doCacheResultSet()
    {
        CacheResource resource = cache.lookup(queryString);

        ResultSet rs;
        if(resource == null || resource.isOutdated()) {

            try {
                rs = getDecoratee().execSelect();
            } catch(Exception e) {
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    //logger.trace("Cache hit for " + queryString);
                    return resource.asResultSet();
                } else {
                    throw new RuntimeException(e);
                }
            }

            logger.trace("Cache write: " + queryString);
            cache.write(queryString, rs);
            resource = cache.lookup(queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }

        } else {
            logger.trace("Cache hit: " + queryString);
        }

        return resource.asResultSet();
    }

    public Model doCacheModel(Model result, ModelProvider modelProvider) {
        try {
            return _doCacheModel(result, modelProvider);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Model _doCacheModel(Model result, ModelProvider modelProvider) throws IOException {
        CacheResource resource = cache.lookup(queryString);

        Model model = ModelFactory.createDefaultModel();
        if(resource == null || resource.isOutdated()) {

            try {
                model = modelProvider.getModel(); //getDecoratee().execConstruct();
            } catch(Exception e) {
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    model = resource.asModel(model);
                    result.add(model);
                    return result;
                } else {
                    throw new RuntimeException(e);
                }
            }

            logger.trace("Cache write: " + queryString);
            cache.write(queryString, model);
            resource = cache.lookup(queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }
        } else {
            logger.trace("Cache hit: " + queryString);
        }

        return resource.asModel(result);
    }
    
    public boolean doCacheBoolean()
    {
        CacheResource resource = cache.lookup(queryString);

        boolean ret;
        if(resource == null || resource.isOutdated()) {

            try {
                ret = getDecoratee().execAsk();
            } catch(Exception e) {
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    //logger.trace("Cache hit for " + queryString);
                    return resource.asBoolean();
                } else {
                    throw new RuntimeException(e);
                }
            }

            logger.trace("Cache write: " + queryString);
            cache.write(queryString, ret);
            resource = cache.lookup(queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }

        } else {
            logger.trace("Cache hit: " + queryString);
        }

        return resource.asBoolean();
    }


    @Override
     public ResultSet execSelect() {
        return doCacheResultSet();
     }

    @Override
    public Model execConstruct() {
        return execConstruct(ModelFactory.createDefaultModel());
    }

    @Override
    public Model execConstruct(Model model) {
       return doCacheModel(model, new ModelProvider() {
           @Override
           public Model getModel() {
               return getDecoratee().execConstruct();
           }
       });
    }

    @Override
    public Model execDescribe() {
        return execDescribe(ModelFactory.createDefaultModel());
    }

    @Override
    public Model execDescribe(Model model) {
        return doCacheModel(model, new ModelProvider() {
            @Override
            public Model getModel() {
                return getDecoratee().execDescribe();
            }
        });
    }

     @Override
     public boolean execAsk() {
         return doCacheBoolean();
     }
}
