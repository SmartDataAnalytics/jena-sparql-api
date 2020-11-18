package org.aksw.jena_sparql_api.io.binseach;

import java.util.Iterator;

import org.aksw.jena_sparql_api.rx.GraphFactoryEx;
import org.aksw.jena_sparql_api.rx.GraphOpsRx;
import org.aksw.jena_sparql_api.rx.query_flow.QueryFlowOps;
import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.apache.jena.ext.com.google.common.cache.Cache;
import org.apache.jena.ext.com.google.common.cache.CacheBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GraphFromSubjectCache
    extends GraphBase
{
    protected Graph delegate;
    protected Cache<Node, Graph> subjectCache;
//    protected Map<Node, Graph> subjectCache = new LRUMap<>(1000);

    public GraphFromSubjectCache(Graph delegate) {//, CacheBuilder<Node, Graph> subjectCacheBuilder) {
        this(delegate, CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(1000)
                .concurrencyLevel(1)
                .build());
    }

    public GraphFromSubjectCache(Graph delegate, Cache<Node, Graph> cache) {//, CacheBuilder<Node, Graph> subjectCacheBuilder) {
        super();
        this.delegate = delegate;
        this.subjectCache = cache;
    }

    public Cache<Node, Graph> getSubjectCache() {
//        return null;
        return subjectCache;
    }

    protected Graph loadGraph(Node s) {
//        System.err.println("Cache miss for " + s);
        Graph result = QueryFlowOps.createFlowableFromGraph(delegate, Triple.create(s, Node.ANY, Node.ANY))
                .compose(GraphOpsRx.graphFromConsecutiveTriples(Triple::getSubject, GraphFactory::createDefaultGraph))
                .blockingFirst(Graph.emptyGraph);

        System.err.println("Cache miss for " + s + "; loaded " + result.size() + " triples - cache size " + subjectCache.size());
        return result;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        // For any triple pattern with a concrete subject, load all triples from the underlying graph
        Node s = triplePattern.getSubject();

        Flowable<Graph> graphFlow;
        if(s.isConcrete()) {
            Graph g;
            try {
//                if(subjectCache.getIfPresent(s) != null) {
//                    System.err.println("Cache hit for " + s + " cache size " + subjectCache.size());
//                }
//                g = subjectCache.computeIfAbsent(s,  x -> loadGraph(x));
                g = subjectCache.get(s, () -> loadGraph(s));
//            } catch (ExecutionException e1) {
              } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
            graphFlow = g == null ? Flowable.empty() : Flowable.just(g);
        } else {
            Triple surrogatePattern = Triple.create(triplePattern.getSubject(), Node.ANY, Node.ANY);
            //delegate.find(surrogatePattern);

            // TODO We create an rx flow just to eventually create an iterator from it
            // The only reason we cannot get rid of rx here right now is because of
            // groupConsecutiveTriplesRaw which does not fit into Jena's iterator machinery
            // We could create a java stream based version and use it with flatMap
            graphFlow = QueryFlowOps.createFlowableFromGraph(delegate, surrogatePattern)
                .subscribeOn(Schedulers.io())
                // Insert order preserving graphs should be more light weight because they only index by s/p/o
                .compose(GraphOpsRx.groupConsecutiveTriplesRaw(Triple::getSubject, GraphFactoryEx::createInsertOrderPreservingGraph))
//                .compose(GraphOpsRx.groupConsecutiveTriplesRaw(Triple::getSubject, GraphFactory::createDefaultGraph))
                .map(e -> {
                    Node key = e.getKey();
                    Graph g = e.getValue();

//                    if(key.toString().contains("https://d-nb.info/gnd/82890-7/about")) {
//                        System.out.println("Scanned graph " + key + " with size " + g.size() + " cache size: " + subjectCache.size() + " cache stats: " + subjectCache.stats());
//                    }
//                    System.err.println("Putting: " + key);
                    subjectCache.put(key, g);
                    return e.getValue();
                })
                ;
                //.map(Entry::getValue);
        }

        Flowable<Triple> resultFlow = graphFlow
            .flatMap(g -> QueryFlowOps.createFlowableFromGraph(g, triplePattern)
                    .filter(candidate -> {
                        boolean r = triplePattern.matches(candidate);
                        return r;
                    }))
            ;

        Iterator<Triple> itTriples = resultFlow
                .blockingIterable(1).iterator();

        ExtendedIterator<Triple> result = ExtendedIteratorClosable.create(itTriples, () -> {
            ((Disposable)itTriples).dispose();
        });

        // System.err.println("Cache stats: " + subjectCache.stats());
        return result;
    }


//    public static Flowable<Triple> createFlowableFromGraph(Graph g, Triple pattern) {
//        // System.out.println("  Flow from " + pattern);
//        return Flowable.<Triple, ExtendedIterator<Triple>>generate(
//                () -> g.find(pattern),
//                (state, emitter) -> {
//                    if(state.hasNext()) {
//                        Triple t = state.next();
//                        emitter.onNext(t);
//                    } else {
//                        emitter.onComplete();
//                    }
//                },
//                ExtendedIterator::close);
//    }

    @Override
    public boolean isClosed() {
        boolean result = delegate.isClosed();
        return result;
    }

    @Override
    public void close() {
        delegate.close();
    }
}
