package org.aksw.jena_sparql_api.io.binseach;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.aksw.jena_sparql_api.rx.GraphOpsRx;
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

    public GraphFromSubjectCache(Graph delegate) {//, CacheBuilder<Node, Graph> subjectCacheBuilder) {
        super();
        this.delegate = delegate;
        this.subjectCache = CacheBuilder.newBuilder().recordStats().maximumSize(1000).build();
    }

    public Cache<Node, Graph> getSubjectCache() {
        return subjectCache;
    }

    protected Graph loadGraph(Node s) {
        Graph result = createFlowableFromGraph(delegate, Triple.create(s, Node.ANY, Node.ANY))
                .compose(GraphOpsRx.graphFromConsecutiveTriples(Triple::getSubject, GraphFactory::createDefaultGraph))
                .blockingFirst(Graph.emptyGraph);

//        System.err.println("Needed to load " + s + " with size " + result.size());
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
                g = subjectCache.get(s, () -> loadGraph(s));
            } catch (ExecutionException e1) {
                throw new RuntimeException(e1);
            }
            graphFlow = g == null ? Flowable.empty() : Flowable.just(g);
        } else {
            Triple surrogatePattern = Triple.create(triplePattern.getSubject(), Node.ANY, Node.ANY);
            //delegate.find(surrogatePattern);
            graphFlow = createFlowableFromGraph(delegate, surrogatePattern)
                .subscribeOn(Schedulers.io())
                .compose(GraphOpsRx.groupConsecutiveTriplesRaw(Triple::getSubject, GraphFactory::createDefaultGraph))
                .doOnNext(e -> {
                    Node key = e.getKey();
                    Graph g = e.getValue();

//                    if(key.toString().contains("https://d-nb.info/gnd/82890-7/about")) {
//                        System.out.println("Scanned graph " + key + " with size " + g.size() + " cache size: " + subjectCache.size() + " cache stats: " + subjectCache.stats());
//                    }
                    subjectCache.put(key, g);
                })
                .map(Entry::getValue);
        }

        Flowable<Triple> resultFlow = graphFlow
            .flatMap(g -> createFlowableFromGraph(g, triplePattern)
                    .filter(triplePattern::matches))
            ;

        Iterator<Triple> itTriples = resultFlow
                //.observeOn(Schedulers.computation())
                .blockingIterable().iterator();

        ExtendedIterator<Triple> result = ExtendedIteratorClosable.create(itTriples, () -> {
            ((Disposable)itTriples).dispose();
        });

        // System.err.println("Cache stats: " + subjectCache.stats());
        return result;
    }


    public static Flowable<Triple> createFlowableFromGraph(Graph g, Triple pattern) {
        // System.out.println("  Flow from " + pattern);
        return Flowable.<Triple, ExtendedIterator<Triple>>generate(
                () -> g.find(pattern),
                (state, emitter) -> {
                    if(state.hasNext()) {
                        Triple t = state.next();
                        emitter.onNext(t);
                    } else {
                        emitter.onComplete();
                    }
                },
                ExtendedIterator::close);
    }

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
