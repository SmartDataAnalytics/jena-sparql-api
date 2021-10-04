package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collection.observable.CollectionChangedEvent;
import org.aksw.commons.collection.observable.CollectionChangedEventImpl;
import org.aksw.commons.collection.observable.ObservableCollectionOps;
import org.aksw.commons.collection.observable.StreamOps;
import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.jena_sparql_api.relation.TripleConstraint;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

import com.google.common.collect.Sets;



/**
 * This is a mutable graph view based on filtering a delegate {@link ObservableGraph}'s triples.
 * Listeners registered on this class are wrapped with a filtering listener that gets registered
 * on the delegate.
 *
 * All access and modification methods (add/delete/remove/find/clear) only affect the set of triples
 * that match the given {@link TripleConstraint}.
 * Addition of triples for which the predicate tests to false are silently discarded.
 *
 *
 * @author raven
 *
 */
public class ObservableSubGraph
    extends GraphWithFilter
    implements ObservableGraph
{
//    public ObservableSubGraph(ObservableGraph graph, Predicate<? super Triple> predicate) {
//        super(graph, predicate);
//    }
//
//    public static ObservableSubGraph decorate(ObservableGraph graph, Predicate<? super Triple> predicate) {
//        return new ObservableSubGraph(graph, predicate);
//    }

    public ObservableSubGraph(ObservableGraph graph, TripleConstraint predicate) {
        super(graph, predicate);
    }

    public static ObservableSubGraph decorate(ObservableGraph graph, TripleConstraint predicate) {
        return new ObservableSubGraph(graph, predicate);
    }

    @Override
    public ObservableGraph get() {
        return (ObservableGraph)super.get();
    }

    @Override
    public boolean delta(Collection<? extends Triple> rawAdditions, Collection<?> rawDeletions) {
        Collection<Triple> filteredRemovals = StreamOps.<Triple>filter(rawDeletions.stream(), predicate).collect(Collectors.toSet());
        Collection<Triple> filteredAdditions = rawAdditions.stream().filter(predicate).collect(Collectors.toSet());

        return get().delta(filteredAdditions, filteredRemovals);
    }

//    @Override
//    public boolean replace(Set<? extends Triple> triples) {
//        Set<Triple> old = find().toSet();
//
//        Set<Triple> removals = Sets.difference(old, triples);
//        Set<? extends Triple> additions = Sets.difference(triples, old);
//
//
//    }

    public static <T> Set<T> filterSet(Set<T> set, Predicate<? super T> predicate) {
        return set == null ? null : Sets.filter(set, predicate::test);
    }

    public static CollectionChangedEvent<Triple> filter(Object self,
            CollectionChangedEvent<Triple> ev, TripleConstraint tripleConstraint) {
        return new CollectionChangedEventImpl<>(
            self,
            new GraphWithFilter((Graph)ev.getOldValue(), tripleConstraint),
            new GraphWithFilter((Graph)ev.getNewValue(), tripleConstraint),
            filterSet((Set<Triple>)ev.getAdditions(), tripleConstraint),
            filterSet((Set<Triple>)ev.getDeletions(), tripleConstraint),
            filterSet((Set<Triple>)ev.getRefreshes(), tripleConstraint));
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        return get().addVetoableChangeListener(ev -> {
            CollectionChangedEvent<Triple> newEv = filter(this, (CollectionChangedEvent<Triple>)ev, predicate);

            if (newEv.hasChanges()) {
                listener.vetoableChange(newEv);
            }
        });

    }


    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return get().addPropertyChangeListener(ev -> {
            CollectionChangedEvent<Triple> newEv = filter(this, (CollectionChangedEvent<Triple>)ev, predicate);

            if (newEv.hasChanges()) {
                listener.propertyChange(newEv);
            }
        });
    }
}
