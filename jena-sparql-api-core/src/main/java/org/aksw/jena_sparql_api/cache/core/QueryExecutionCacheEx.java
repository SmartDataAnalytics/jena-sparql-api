package org.aksw.jena_sparql_api.
cache.core;

import java.io.IOException;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:11 PM
 */
public class QueryExecutionCacheEx
    extends QueryExecutionDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCacheEx.class);


    private CacheFrontend cache;
    private String service;
    private String queryString;

    
    // The cache resource - created upon execution of a query
    private CacheResource currentResource = null;
    
    public QueryExecutionCacheEx(QueryExecution decoratee, String service, String queryString, CacheFrontend cache) {
        super(decoratee);

        this.service = service;
        this.queryString = queryString;
        this.cache = cache;
    }

    /**
     * Helper function that closes outdated resources
     * 
     * @param resource
     * @return
     */
    public static boolean needsCaching(CacheResource resource) {
        boolean result;

        if(resource == null) {
            result = true;
        }
        else if(resource.isOutdated()) {
            resource.close();
            result = true;
        }
        else {
            result = false;
        }
        
        return result;
    }

    public synchronized ResultSet doCacheResultSet()
    {
        CacheResource resource = cache.lookup(service, queryString);

        ResultSet rs;
        if(needsCaching(resource)) {

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

        currentResource = resource;
        
        return resource.asResultSet();
    }

    public synchronized Model doCacheModel(Model result, ModelProvider modelProvider) {
        try {
            return _doCacheModel(result, modelProvider);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Model _doCacheModel(Model result, ModelProvider modelProvider) throws IOException {
        CacheResource resource = cache.lookup(service, queryString);

        if(needsCaching(resource)) {
            
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
    
    public synchronized boolean doCacheBoolean()
    {
        /*
        if(true) {
            return false;
        }
        */
        CacheResource resource = cache.lookup(service, queryString);

        boolean ret;
        if(needsCaching(resource)) {
            
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
     
     
     @Override
     public void close() {
         if(currentResource != null) {
             currentResource.close();
         }
         
//         if(currentCloseAction != null) {
//             currentCloseAction.close();
//         }
     }
}
