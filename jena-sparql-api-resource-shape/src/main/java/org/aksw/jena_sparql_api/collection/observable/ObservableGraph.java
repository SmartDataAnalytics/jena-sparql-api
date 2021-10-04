package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueFromObservableCollection;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public interface ObservableGraph
    extends Graph
{
    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);

    boolean delta(Collection<? extends Triple> rawAdditions, Collection<?> rawDeletions);

//    boolean replace(Set<? extends Triple> triples);

    default ObservableSet<Triple> asSet() {
        return new ObservableSetFromGraph(this);
    }


    default ObservableValue<Node> createValueField(Node source, Node predicate, boolean isForward) {
        DirectedFilteredTriplePattern dftp = DirectedFilteredTriplePattern.create(source, predicate, isForward);
        ObservableValue<Node> result = createValueField(source, dftp);
        return result;
    }

    default ObservableValue<Node> createValueField(Node sourceNode, DirectedFilteredTriplePattern dftp) {
        ObservableCollection<Node> set = createSetField(sourceNode, dftp);
        ObservableValue<Node> result = ObservableValueFromObservableCollection.decorate(set);
        return result;
    }

    default ObservableCollection<Node> createSetField(Node sourceNode, DirectedFilteredTriplePattern dftp) {
        ObservableCollection<Node> set = SetOfNodesFromGraph.create(this, dftp);
        return set;
    }

    default ObservableCollection<Node> createSetField(Node source, Node predicate, boolean isForward) {
        DirectedFilteredTriplePattern dftp = DirectedFilteredTriplePattern.create(source, predicate, isForward);
        return createSetField(source, dftp);
    }

}
