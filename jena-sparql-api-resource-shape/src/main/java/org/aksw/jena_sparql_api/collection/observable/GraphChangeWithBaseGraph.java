package org.aksw.jena_sparql_api.collection.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableMap;
import org.aksw.commons.collection.observable.ObservableMapImpl;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueFromObservableCollection;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.aksw.jena_sparql_api.util.SetFromGraph;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Multimaps;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class GraphChangeWithBaseGraph
    extends GraphChange
{
    protected ObservableGraph baseGraph;

    public GraphChangeWithBaseGraph() {
        this(ObservableMapImpl.decorate(new HashMap<>()), ObservableMapImpl.decorate(new HashMap<>()), ObservableGraphImpl.decorate(GraphFactory.createPlainGraph()));
        //new Delta(GraphFactory.createPlainGraph()));
    }


    public GraphChangeWithBaseGraph(ObservableMap<Node, Node> renamedNodes, ObservableMap<Triple, Triple> tripleReplacements, ObservableGraph baseGraph) {
        super(renamedNodes, tripleReplacements);
//        this.renamedNodes = renamedNodes;
//        this.tripleReplacements = tripleReplacements;
////        this.delta = ObservableGraphImpl.decorate(new DeltaWithFixedIterator(this.baseGraph));
//        //this.effectiveGraph = ObservableGraph
//        this.additionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());
//        this.deletionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());


//        this.effectiveAdditionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());
//        this.effectiveDeletionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());


        tripleReplacements.addPropertyChangeListener(ev -> refreshDeletions());
        additionGraph.addPropertyChangeListener(ev -> refreshDeletions());
        deletionGraph.addPropertyChangeListener(ev -> refreshDeletions());
        renamedNodes.addPropertyChangeListener(ev -> refreshDeletions());

        baseGraph.addPropertyChangeListener(ev -> refreshDeletions());

    }

    public ObservableGraph getBaseGraph() {
        return baseGraph;
    }



    public ObservableCollection<Node> createSetField(Node sourceNode, DirectedFilteredTriplePattern dftp) {

        ObservableCollection<Node> set = SetOfNodesFromGraph.create(baseGraph, dftp);
        return set;
    }

    public ObservableValue<Node> createValueField(Node sourceNode, DirectedFilteredTriplePattern dftp) {
        ObservableCollection<Node> set = createSetField(sourceNode, dftp);
        ObservableValue<Node> result = ObservableValueFromObservableCollection.decorate(set);
        return result;
    }


    /**
     * Return a graph view where all attributes of resources that are renamed
     * to the same final resource appear on all involved resources.
     *
     * This graph view differs from the effective graph view where the resources
     * that are the source of renaming do no longer exist (as they have been renamed)
     *
     * @return
     */
    public Graph getSameAsInferredGraphView() {
        return new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {

                Map<Node, Node> nodeToCluster = new HashMap<>(renamedNodes);
                // For each value that is not mapped to by a key map it to itself
                for (Node v : renamedNodes.values()) {
                    Node newV = renamedNodes.get(v);

                    if (newV == null) {
                        nodeToCluster.put(v,  v);
                    }
                }


                Multimap<Node, Node> clusterToMembers = nodeToCluster.entrySet().stream()
                        .collect(Multimaps.toMultimap(Entry::getValue, Entry::getKey, HashMultimap::create));

//                Multimap<Node, Node> fwdMap = Multimaps.forMap(map);

                // For each value that is not mapped to by a key map it to itself
//                for (Node v : renamedNodes.values()) {
//                    Node newV = renamedNodes.get(v);
//
//                    if (newV == null) {
//                        fwdMap.put(v,  v);
//                    }
//                }

                Stream<Triple> expandedLookups = expand(triplePattern, Triple.createMatch(null, null, null),  node -> clusterToMembers.get(nodeToCluster.get(node)));

//                Stream<Triple> expandedLookups = Streams.concat(
//                    Stream.of(triplePattern),
//                    extraLookups);

                List<Triple> tmpX = expandedLookups.collect(Collectors.toList());
                expandedLookups = tmpX.stream();
//                System.out.println("Expanded " + triplePattern + " to " + tmpX);

                Stream<Triple> rawTriples = expandedLookups
                        .flatMap(pattern -> Streams.stream(baseGraph.find(pattern)));

                Stream<Triple> stream = rawTriples
                    .flatMap(triple -> {

                        Stream<Triple> r;

                        boolean isRemapped = tripleReplacements.containsKey(triple);
                        if (isRemapped) {
                            Triple replacement = tripleReplacements.get(triple);
                            r = replacement == null
                                    ? Stream.empty()
                                    : Stream.of(replacement);
                        } else {
                            r = Stream.of(triple);
                        }

                        return r;
                    })
                    .flatMap(triple -> {
                        return expand(triple, triplePattern, node -> clusterToMembers.get(nodeToCluster.get(node)));
                    });

                List<Triple> tmp = stream.collect(Collectors.toList());
                stream = tmp.stream();

//                System.out.println("Lookup for " + triplePattern);
//                System.out.println("Returned: " + tmp);

                ExtendedIterator<Triple> result = WrappedIterator.create(stream.iterator());
                return result;
            }
        };
    }


    /**
     * A graph view of the final state:
     * - Nodes that were renamed are no longer visible.
     *
     * @return
     */
    public Graph getEffectiveGraphView() {
        return new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {

                // If there is a request for x but x was renamed to y
                // then rephrase the express in terms of y.

                Map<Node, Node> nodeToCluster = new HashMap<>(renamedNodes);
                // For each value that is not mapped to by a key map it to itself
                for (Node v : renamedNodes.values()) {
                    Node newV = renamedNodes.get(v);

                    if (newV == null) {
                        nodeToCluster.put(v,  v);
                    }
                }
                Multimap<Node, Node> clusterToMembers = nodeToCluster.entrySet().stream()
                        .collect(Multimaps.toMultimap(Entry::getValue, Entry::getKey, HashMultimap::create));

//                Multimap<Node, Node> fwdMap = Multimaps.forMap(renamedNodes);

                // If a node was renamed it ceases to exist
                Stream<Triple> expandedLookups = expand(triplePattern, Triple.createMatch(null, null, null),
                        x -> {
                            Collection<Node> sources = clusterToMembers.get(x);
                            Collection<Node> r = !sources.isEmpty()
                                    ? sources
                                    : renamedNodes.containsKey(x)
                                        ? sources
                                        : Collections.singleton(x);
                            return r;
                        });
//                        x -> renamedNodes.get(x) != null ? Collections.emptySet() : clusterToMembers.get(x));


                List<Triple> tmpX = expandedLookups.collect(Collectors.toList());
                expandedLookups = tmpX.stream();
//                System.out.println("Expanded " + triplePattern + " to " + tmpX);

                Stream<Triple> rawTriples = expandedLookups
                        .flatMap(pattern -> Streams.stream(baseGraph.find(pattern)));

                Stream<Triple> stream = rawTriples
                    .flatMap(triple -> {

                        Stream<Triple> r;

                        boolean isRemapped = tripleReplacements.containsKey(triple);
                        if (isRemapped) {
                            Triple replacement = tripleReplacements.get(triple);
                            r = replacement == null
                                    ? Stream.empty()
                                    : Stream.of(replacement);
                        } else {
                            r = Stream.of(triple);
                        }

                        return r;
                    })
                    .flatMap(triple -> {
                        return expand(triple, triplePattern,
                                x -> nullableSingleton(renamedNodes.getOrDefault(x, x)));
                    });

                List<Triple> tmp = stream.collect(Collectors.toList());
                stream = tmp.stream();

//                System.out.println("Lookup for " + triplePattern);
//                System.out.println("Returned: " + tmp);

                ExtendedIterator<Triple> result = WrappedIterator.create(stream.iterator());
                return result;
            }
        };
    }


    protected void refreshDeletions() {
        Set<Triple> additions = new LinkedHashSet<>();
        Set<Triple> deletions = new LinkedHashSet<>();
//        SetDiff<Triple> diff = new SetDiff<>(new HashSet<>(), new HashSet<>());

        deletionGraph.find().forEachRemaining(deletions::add);

        {
            Iterator<Triple> itTriple = baseGraph.find();
            while (itTriple.hasNext()) {
                Triple t = itTriple.next();

                for (RdfField field : sourceNodeToField.values()) {
                    if (field.isIntensional() && field.isDeleted()) {
                        if (field.matchesTriple(t)) {
                            deletions.add(t);
                        }
                    }
                }
            }
        }

        Set<Triple> keys = tripleReplacements.keySet();
        deletions.addAll(keys);

        Set<Triple> valueSet = new HashSet<>(tripleReplacements.values());
        deletions.removeAll(valueSet);


        additionGraph.find().forEachRemaining(additions::add);
        valueSet.stream()
            .filter(item -> item != null && !baseGraph.contains(item))
            .forEach(additions::add);

        NodeTransform xform = n -> {
            Node r = renamedNodes.get(n);
            return r == null ? n : r;
        };
        additions = additions.stream().map(t -> NodeTransformLib.transform(xform, t)).collect(Collectors.toSet());

        makeSetEqual(new SetFromGraph(effectiveDeletionGraph), deletions);
        makeSetEqual(new SetFromGraph(effectiveAdditionGraph), additions);


//        {
//            NodeTransform xform = new NodeTransformRenameMap(renamedNodes);
//            Iterator<Triple> itTriple = baseGraph.find();
//            while (itTriple.hasNext()) {
//                Triple t = itTriple.next();
//                Triple remapped = NodeTransformLib.transform(xform, t);
//
//
//
//
//            }
//        }
//
//        Collection<Triple> values = tripleReplacements.values();
//        values.stream().filter(Objects::nonNull).forEach(delta::add);

    }

    /** Return a set view over the values of a given predicate.
     * Adding items to the set creates new triples.
     *
     * TODO Maybe the result should not be an ObservableSet directly but a GraphNode that supports
     * the set view and e.g. a triple based view
     **/
    public ObservableCollection<Node> createSetForPredicate(Node source, Node predicate, boolean isForward) {
        DirectedFilteredTriplePattern dftp = DirectedFilteredTriplePattern.create(source, predicate, isForward);
        return createSetField(source, dftp);
    }



}
