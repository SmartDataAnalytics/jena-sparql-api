package org.aksw.jena_sparql_api.cache.staging;

import java.io.IOException;

import org.aksw.jena_sparql_api.arq.core.query.QueryExecutionDecoratorBase;
import org.aksw.jena_sparql_api.cache.core.ModelProvider;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheResource;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:11 PM
 */
public class QueryExecutionCacheFrontend
    extends QueryExecutionDecoratorBase<QueryExecution>
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCacheFrontend.class);

    private CacheFrontend cacheFrontend;
    private String service;
    private String queryString;


    private CacheResource resource;

    public QueryExecutionCacheFrontend(QueryExecution decoratee, String service, String queryString, CacheFrontend cacheFrontend) {
        super(decoratee);

        this.service = service;
        this.queryString = queryString;
        this.cacheFrontend = cacheFrontend;
    }

    private void setResource(CacheResource resource) {
        if(this.resource != null) {
            this.resource.close();

            if(resource != null) {
                resource.close();
            }
            //throw new RuntimeException("Attempted to set a resource while another one was in use");
        }

        this.resource = resource;
    }

    public synchronized ResultSet doCacheResultSet()
    {
        // TODO I think we can now remove the synchronized blocks, as the
        // CacheBackend does synchronization

        synchronized(this) {
            resource = cacheFrontend.lookup(service, queryString);
            setResource(resource);
        }

        ResultSet rs;
        if(resource == null || resource.isOutdated()) {

            if(resource != null) {
                resource.close();
            }

            try {
                rs = getDelegate().execSelect();
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

            if(!cacheFrontend.isReadOnly()) {

                logger.trace("Cache write [" + service + "]: " + queryString);

                // TODO We need to get a promise for the write action so we can cancel it
                cacheFrontend.write(service, queryString, rs);

                synchronized(this) {
                    resource = cacheFrontend.lookup(service, queryString);
                    setResource(resource);
                }

                if(resource == null) {
                    throw new RuntimeException("Cache error: Lookup of just written data failed");
                }
            }
        } else {
            logger.trace("Cache hit [" + service + "]:" + queryString);
        }

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
        synchronized(this) {
            resource = cacheFrontend.lookup(service, queryString);
            setResource(resource);
        }

        if(resource == null || resource.isOutdated()) {

            Model model;
            try {
                model = modelProvider.getModel(); //getDelegate().execConstruct();
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
            cacheFrontend.write(service, queryString, model);
            synchronized(this) {
                resource = cacheFrontend.lookup(service, queryString);
                setResource(resource);
            }
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
        synchronized(this) {
            resource = cacheFrontend.lookup(service, queryString);
            setResource(resource);
        }

        boolean ret;
        if(resource == null || resource.isOutdated()) {

            try {
                ret = getDelegate().execAsk();
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
            cacheFrontend.write(service, queryString, ret);
            synchronized(this) {
                resource = cacheFrontend.lookup(service, queryString);
                setResource(resource);
            }
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
                return getDelegate().execConstruct();
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
                 return getDelegate().execDescribe();
             }
         });
     }

     @Override
     public boolean execAsk() {
         return doCacheBoolean();
     }

     @Override
     public void abort() {
         if(resource != null) {
             resource.close();
         }

         // TODO We also need to close writes!

         super.abort();
     }

}

