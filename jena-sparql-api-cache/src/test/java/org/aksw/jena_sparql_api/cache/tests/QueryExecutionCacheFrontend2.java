package org.aksw.jena_sparql_api.cache.tests;

/**
 * Implementation that does background caching
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/16
 *         Time: 4:11 PM
 */
//public class QueryExecutionCacheFrontend2<K>
//    extends QueryExecutionDecorator
//{
//    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCacheFrontend2.class);
//
//    protected CacheFrontend2<? super K> cacheFrontend;
//    protected K key;
//    protected ExecutorService executorService;
//
//    protected boolean stopCachingOnAbort;
//
//    protected transient CacheResource resource;
//    protected transient Runnable closeAction;
//
//
//    public QueryExecutionCacheFrontend2(
//            QueryExecution decoratee,
//            CacheFrontend2<? super K> cacheFrontend,
//            K key,
//            ExecutorService executorService
//    ) {
//        super(decoratee);
//
//        this.cacheFrontend = cacheFrontend;
//        this.key = key;
//        this.executorService = executorService;
//    }
//
//    private void setResource(CacheResource resource) {
//        if(this.resource != null) {
//            this.resource.close();
//
//            if(resource != null) {
//                resource.close();
//            }
//            //throw new RuntimeException("Attempted to set a resource while another one was in use");
//        }
//
//        this.resource = resource;
//    }
//
//    public synchronized ResultSet doCacheResultSet()
//    {
//        // TODO We may need to synchronize on the cacheFrontend for the whole process
//        resource = cacheFrontend.lookup(key);
//        setResource(resource);
//
//        ResultSet result;
//        if(resource == null || resource.isOutdated()) {
//
//            if(resource != null) {
//                resource.close();
//            }
//
//            ResultSet coreRs;
//            try {
//                coreRs = getDecoratee().execSelect();
//            } catch(Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            logger.trace("Cache write [" + key + "]");
//
//            // Note: A cache execution may already be in progress
//            List<Binding> cacheData = new ArrayList<>();
//            Cache<List<Binding>> cache = new Cache<>(cacheData);
//
//            List<String> rsVars = coreRs.getResultVars();
//
//            //Runnable
//            Runnable task = () -> {
//                while(coreRs.hasNext() && !cache.isAbanoned()) {
//                    Binding binding = coreRs.nextBinding();
//                    cacheData.add(binding);
//                }
//
//                if(!cache.isAbanoned()) {
//                    cache.setComplete(true);
//                }
//            };
//            Future<?> future = executorService.submit(task);
//
//
//
//            BlockingCacheIterator<Binding> it = new BlockingCacheIterator<>(cache);
//            QueryIterator queryIt = new QueryIterPlainWrapper(it);
//            result = ResultSetFactory.create(queryIt, rsVars);
//
//            //result = ResultSetClose.
//
//
//            // Let the cache take care of writing
//            cacheFrontend.write(key, coreRs);
//
//        } else {
//            logger.trace("Cache hit [" + key + "]");
//            result = resource.asResultSet();
//        }
//
//        return result;
//    }
//
//
////    synchronized(this) {
////        resource = cacheFrontend.lookup(key);
////        setResource(resource);
////    }
////
////    if(resource == null) {
////        throw new RuntimeException("Cache error: Lookup of just written data failed");
////    }
//
//
//
//    public synchronized Model doCacheModel(Model result, ModelProvider modelProvider) {
//        try {
//            return _doCacheModel(result, modelProvider);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public synchronized Model _doCacheModel(Model result, ModelProvider modelProvider) throws IOException {
//        synchronized(this) {
//            resource = cacheFrontend.lookup(service, queryString);
//            setResource(resource);
//        }
//
//        if(resource == null || resource.isOutdated()) {
//
//            Model model;
//            try {
//                model = modelProvider.getModel(); //getDecoratee().execConstruct();
//            } catch(Exception e) {
//                /*
//                logger.warn("Error communicating with backend", e);
//
//                if(resource != null) {
//                    model = resource.asModel(model);
//                    result.add(model);
//                    return result;
//                } else {
//                    throw new RuntimeException(e);
//                }
//                */
//
//                throw new RuntimeException(e);
//            }
//
//            logger.trace("Cache write [" + service + "]: " + queryString);
//            cacheFrontend.write(service, queryString, model);
//            synchronized(this) {
//                resource = cacheFrontend.lookup(service, queryString);
//                setResource(resource);
//            }
//            if(resource == null) {
//                throw new RuntimeException("Cache error: Lookup of just written data failed");
//            }
//        } else {
//            logger.trace("Cache hit [" + service + "]:" + queryString);
//        }
//
//        return resource.asModel(result);
//    }
//
//    public synchronized boolean doCacheBoolean()
//    {
//        synchronized(this) {
//            resource = cacheFrontend.lookup(service, queryString);
//            setResource(resource);
//        }
//
//        boolean ret;
//        if(resource == null || resource.isOutdated()) {
//
//            try {
//                ret = getDecoratee().execAsk();
//            } catch(Exception e) {
//                /*
//                logger.warn("Error communicating with backend", e);
//
//                if(resource != null) {
//                    //logger.trace("Cache hit for " + queryString);
//                    return resource.asBoolean();
//                } else {
//                    throw new RuntimeException(e);
//                }
//                */
//
//                throw new RuntimeException(e);
//            }
//
//            logger.trace("Cache write [" + key + "]");
//            cacheFrontend.write(service, queryString, ret);
//            synchronized(this) {
//                resource = cacheFrontend.lookup(service, queryString);
//                setResource(resource);
//            }
//            if(resource == null) {
//                throw new RuntimeException("Cache error: Lookup of just written data failed");
//            }
//
//        } else {
//            logger.trace("Cache hit [" + service + "]:" + queryString);
//        }
//
//        return resource.asBoolean();
//    }
//
//
//    @Override
//     public ResultSet execSelect() {
//        return doCacheResultSet();
//     }
//
//     @Override
//     public Model execConstruct() {
//         return execConstruct(ModelFactory.createDefaultModel());
//     }
//
//     @Override
//     public Model execConstruct(Model model) {
//        return doCacheModel(model, new ModelProvider() {
//            @Override
//            public Model getModel() {
//                return getDecoratee().execConstruct();
//            }
//        });
//     }
//
//     @Override
//     public Model execDescribe() {
//         return execDescribe(ModelFactory.createDefaultModel());
//     }
//
//     @Override
//     public Model execDescribe(Model model) {
//         return doCacheModel(model, new ModelProvider() {
//             @Override
//             public Model getModel() {
//                 return getDecoratee().execDescribe();
//             }
//         });
//     }
//
//     @Override
//     public boolean execAsk() {
//         return doCacheBoolean();
//     }
//
//     @Override
//     public void abort() {
//         if(resource != null) {
//             resource.close();
//         }
//
//         if(closeAction != null) {
//             // TODO Add a flag whether this will cancel a cache process
//             closeAction.run();
//         }
//
//         // TODO We also need to close writes!
//
//         super.abort();
//     }
//
//}

