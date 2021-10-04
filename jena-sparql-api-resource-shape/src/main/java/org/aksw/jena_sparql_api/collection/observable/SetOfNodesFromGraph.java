package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableConvertingCollection;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.jena_sparql_api.relation.ConverterTripleToNode;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.base.Converter;


public class SetOfNodesFromGraph {
//    extends AbstractSet<Node>
//    implements ObservableSet<Node> {

    public static ObservableCollection<Node> create(ObservableGraph graph, DirectedFilteredTriplePattern dftp) {
        Node source = dftp.getSource();
        Node predicate = dftp.getTriplePattern().getPredicate();
        boolean isForward = dftp.isForward();

        // TripleConstraint tripleConstraint = TripleConstraintImpl.create(TripleUtils.createMatch(source, predicate, isForward));
        ObservableSubGraph subGraph = ObservableSubGraph.decorate(graph, dftp.toConstraint());

        Converter<Triple, Node> converter = new ConverterTripleToNode(source, predicate, isForward);
        ObservableSet<Triple> tripleSet = new ObservableSetFromGraph(subGraph);
        ObservableCollection<Node> nodeSet = new ObservableConvertingCollection<>(tripleSet, converter);

        return nodeSet;

    }

    public static ObservableCollection<Node> create(ObservableGraph graph, Node sourceNode, Node predicate, boolean isForward) {
        DirectedFilteredTriplePattern dftp = DirectedFilteredTriplePattern.create(sourceNode, predicate, isForward);
        return create(graph, dftp);
    }

//    protected ObservableGraph graph;
//    protected ConverterTripleToNode converter;
//
//    public SetOfNodesFromGraph(ObservableSubGraph graph, ConverterTripleToNode converter) {
//        super();
//        this.graph = graph;
//        this.converter = converter;
//    }
//
//    public SetOfNodesFromGraph create(ObservableGraph graph, Node source, Node predicate, boolean isForward) {
//        TripleConstraint tripleConstraint = TripleConstraintImpl.create(TripleUtils.createMatch(source, predicate, isForward));
//        ObservableSubGraph subGraph = ObservableSubGraph.decorate(graph, tripleConstraint);
//        return new SetOfNodesFromGraph(subGraph, new ConverterTripleToNode(source, predicate, isForward));
//    }
//
//    protected Triple createTriple(Node target) {
//        Triple result = TripleUtils.create(
//                converter.getSource(), converter.getPredicate(),
//                target, converter.isForward());
//        return result;
//    }
//
//    @Override
//    public boolean add(Node e) {
//        boolean result = false;
//        if (!contains(e)) {
//            Triple t = createTriple(e);
//            graph.add(t);
//            result = true;
//        }
//
//        return result;
//    }
//
//    @Override
//    public boolean remove(Object o) {
//        boolean result = false;
//        if (!contains(o)) {
//            Triple t = createTriple(e);
//            graph.add(t);
//            result = true;
//        }
//
//        return result;
//    }
//
//    @Override
//    public boolean contains(Object o) {
//        boolean result = false;
//        if (o instanceof Node) {
//            Node n = (Node) o;
//            Triple t = createTriple(n);
//            result = graph.contains(t);
//        }
//
//        return result;
//    }
//
//    @Override
//    public ExtendedIterator<Node> iterator() {
//        Triple m = TripleUtils.createMatch(converter.getSource(), converter.getPredicate(), converter.isForward());
//
//        ExtendedIterator<Node> result = graph.find(m).mapWith(t -> TripleUtils.getTarget(t, converter.isForward()));
//
//        return result;
//    }
//
//    @Override
//    public int size() {
//        int result = graph.size();
//        // int result = Iterators.size(iterator());
//        return result;
//    }
//
////    protected PropertyChangeEvent convertEvent(PropertyChangeEvent ev) {
////        CollectionChangedEventImpl<Triple> oldEvent = (CollectionChangedEventImpl<Triple>)ev;
////
////        return new CollectionChangedEventImpl<Triple>(
////            this,
////            this,
////            new SetFromGraph((Graph)oldEvent.getNewValue()),
////            oldEvent.getAdditions(),
////            oldEvent.getDeletions(),
////            oldEvent.getRefreshes()
////        );
////    }
//
//    @Override
//    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
//        return graph.addPropertyChangeListener(ev -> {
//            CollectionChangedEventImpl<Triple> oldEv = (CollectionChangedEventImpl<Triple>)ev;
//            PropertyChangeEvent newEv = ObservableConvertingCollection.convertEvent(this, oldEv, converter);
//            if (newEv != null) {
//                listener.propertyChange(newEv);
//            }
//        });
//    }
}
