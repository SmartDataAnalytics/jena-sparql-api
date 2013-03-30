package org.aksw.jena_sparql_api.cache.core;

import java.io.IOException;

import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.aksw.jena_sparql_api.core.QueryExecutionStreamingDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

// TODO Replace with Factory1<Model>
interface ModelProvider
{
    Model getModel();
}

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:11 PM
 */
public class QueryExecutionCacheEx
    extends QueryExecutionStreamingDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCacheEx.class);


    private CacheEx cache;
    private String service;
    private String queryString;

    public QueryExecutionCacheEx(QueryExecutionStreaming decoratee, String service, String queryString, CacheEx cache) {
        super(decoratee);

        this.service = service;
        this.queryString = queryString;
        this.cache = cache;
    }


    public ResultSet doCacheResultSet()
    {
        CacheResource resource = cache.lookup(service, queryString);

        ResultSet rs;
        if(resource == null || resource.isOutdated()) {

            try {
                rs = getDecoratee().execSelect();
            } catch(Exception e) {
                // New strategie:
                // If something goes wrong, just pass the exception on
                // Don't try to return a resource from cache instead

                /*
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    //logger.trace("Cache hit for " + queryString);
                    return resource.asResultSet();
                } else {
                    throw new RuntimeException(e);
                }*/

                throw new RuntimeException(e);
            }

            logger.trace("Cache write [" + service + "]: " + queryString);
            cache.write(service, queryString, rs);
            resource = cache.lookup(service, queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }

        } else {
            logger.trace("Cache hit [" + service + "]:" + queryString);
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
        CacheResource resource = cache.lookup(service, queryString);

        if(resource == null || resource.isOutdated()) {

            Model model;
            try {
                model = modelProvider.getModel(); //getDecoratee().execConstruct();
            } catch(Exception e) {
                /*
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    model = resource.asModel(model);
                    result.add(model);
                    return result;
                } else {
                    throw new RuntimeException(e);
                }
                */

                throw new RuntimeException(e);
            }

            logger.trace("Cache write [" + service + "]: " + queryString);
            cache.write(service, queryString, model);
            resource = cache.lookup(service, queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }
        } else {
            logger.trace("Cache hit [" + service + "]:" + queryString);
        }

        return resource.asModel(result);
    }
    
    public boolean doCacheBoolean()
    {
        CacheResource resource = cache.lookup(service, queryString);

        boolean ret;
        if(resource == null || resource.isOutdated()) {

            try {
                ret = getDecoratee().execAsk();
            } catch(Exception e) {
                /*
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    //logger.trace("Cache hit for " + queryString);
                    return resource.asBoolean();
                } else {
                    throw new RuntimeException(e);
                }
                */

                throw new RuntimeException(e);
            }

            logger.trace("Cache write [" + service + "]: " + queryString);
            cache.write(service, queryString, ret);
            resource = cache.lookup(service, queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }

        } else {
            logger.trace("Cache hit [" + service + "]:" + queryString);
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
