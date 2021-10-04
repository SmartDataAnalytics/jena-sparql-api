package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.collection.observable.ObservableMap;
import org.aksw.commons.collection.observable.ObservableMapImpl;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.commons.collection.observable.ObservableSetImpl;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueFromObservableCollection;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.aksw.jena_sparql_api.util.SetFromGraph;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Multimaps;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import com.google.common.collect.Sets;


class FieldId {
}

class FieldState {
}

public class GraphChange
//    extends GraphBase
{
    /** A set of blank that were newly created and must thus not clash with existing resources */
    // protected Set<Node> newNodes;



    protected ObservableMap<Node, Node> renamedNodes;

    /** Mapping of original triples to their edited versions.
     *  The nodes of the target triple are subject to renaming: If some
     *  source node in a triple gets specifically set to another node, and that other node
     *  is mapped to a new target node
     *  then it seems reasonable to rename the source node to the target one. */
    protected ObservableMap<Triple, Triple> tripleReplacements;


    // protected Table<Node, RdfField, Change> sourceToFieldIdToChanges;
    // protected Table<Node, FieldId, FieldState> sourceToFieldIdToFieldState;
    protected Multimap<Node, RdfField> sourceNodeToField = HashMultimap.create();


//    protected ObservableGraph delta;

    protected ObservableGraph baseGraph;

    /** Explicitly added triples - does not include the values of {@link #renamedNodes} */
    protected ObservableGraph additionGraph;

    /** Explicitly deleted triples */
    protected ObservableGraph deletionGraph;



    /** Effective deletions:
     * The set of triples w.r.t. the base graph that are known to be deleted.
     * This set is the union of the triples that occur in
     * - the base graph that are intensionally deleted by fields
     * - the deletion graph
     * - the key set of the renamed triples
     *
     */
    protected ObservableGraph effectiveDeletionGraph;

    protected ObservableGraph effectiveAdditionGraph;


    /** The set of newly added nodes which should be blank nodes only. These new nodes can
     *  participate in triples and can be renamed. */
    protected ObservableSet<Node> newNodes = new ObservableSetImpl<Node>(new LinkedHashSet<>());

    public ObservableSet<Node> getNewNodes() {
        return newNodes;
    }

    public Node freshNode() {
        Node result = NodeFactory.createBlankNode();
        newNodes.add(result);
        return result;
    }

    public GraphChange() {
        this(ObservableMapImpl.decorate(new HashMap<>()), ObservableMapImpl.decorate(new HashMap<>()), ObservableGraphImpl.decorate(GraphFactory.createPlainGraph()));
        //new Delta(GraphFactory.createPlainGraph()));
    }

    public GraphChange(ObservableMap<Node, Node> renamedNodes, ObservableMap<Triple, Triple> tripleReplacements, ObservableGraph baseGraph) {
        super();
        this.renamedNodes = renamedNodes;
        this.tripleReplacements = tripleReplacements;
        this.baseGraph = baseGraph;
//        this.delta = ObservableGraphImpl.decorate(new DeltaWithFixedIterator(this.baseGraph));
        //this.effectiveGraph = ObservableGraph
        this.additionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());
        this.deletionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());


        this.effectiveAdditionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());
        this.effectiveDeletionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());


        tripleReplacements.addPropertyChangeListener(ev -> refreshDeletions());
        additionGraph.addPropertyChangeListener(ev -> refreshDeletions());
        deletionGraph.addPropertyChangeListener(ev -> refreshDeletions());
        baseGraph.addPropertyChangeListener(ev -> refreshDeletions());
        renamedNodes.addPropertyChangeListener(ev -> refreshDeletions());
    }

    /**
     * Puts an entry into the rename map with the following extra rule:
     * If both arguments are equal (a rename of a node to itself) then the rename entry is removed.
     *
     * @param before
     * @param after
     * @return
     */
    public GraphChange putRename(Node before, Node after) {
        Map<Node, Node> renames = getRenamedNodes();
        if (Objects.equals(before, after)) {
            renames.remove(before);
        } else {
            renames.put(before, after);
        }

        return this;
    }

    public ObservableGraph getEffectiveAdditionGraph() {
        return effectiveAdditionGraph;
    }

    public ObservableGraph getEffectiveDeletionGraph() {
        return effectiveDeletionGraph;
    }

    public boolean isDeleted(Triple triple) {

        for (int i = 0; i < 2; ++i) {
            Node sourceNode = i == 0 ? triple.getSubject() : triple.getObject();
            boolean fwd = i == 0;

            // Find all fields that would match the triple
            sourceNodeToField.get(sourceNode).stream()
                .filter(field -> field.getSourceNode().equals(sourceNode) && field.getPropertySchema().isForward() == fwd);

            // Check whether the field is marked as deleted



        }
        // Collection<RdfField> rdfFields = source

//    	triple.getSubject()
        return false;
    }

    //protected Map<Node, > ;

    /** Replacing a triple with null counts as a deletion */
    // protected Set<Triple> tripleDeletions;

    //protected Map<Object, RdfField> fieldKeyToField;
    // protected Set<RdfField> fields;

//    protected Multimap<Node, PropertySchema> nodeTo;

    protected PropertyChangeSupport pce = new PropertyChangeSupport(this);

    /** Listeners for after changes occurred. This allows listeners to update their state */
//    protected PropertyChangeSupport postUpdateListeners = new PropertyChangeSupport(this);


//    public void triggerPostUpdate() {
//        postUpdateListeners.firePropertyChange("status", null, null);
//    }
//
//    public Runnable addPostUpdateListener(PropertyChangeListener listener) {
//        postUpdateListeners.addPropertyChangeListener(listener);
//        return () -> postUpdateListeners.removePropertyChangeListener(listener);
//    }


    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pce.addPropertyChangeListener(listener);
        return () -> pce.removePropertyChangeListener(listener);
    }


    // Do we need a deletion graph?
//    protected ObservableGraph deletionGraph;

    /**
     * Create a reference to a specific triple such that one of its components
     * can be modified.
     * Note that node remapping is applied after the triple remapping.
     *
     * - Fields that delete the original triple are NOT in conflict.
     * - Fields that delete the target triple AFTER node remapping ARE in conflict
     * 		I.e. the result of editing must not result in a newly added triple being immediately deleted again
     *
     * @param baseTriple
     * @param componentIdx
     * @return
     */
    public ObservableValue<Node> createFieldForExistingTriple(Triple baseTriple, int componentIdx) {
        return new RdfFieldFromExistingTriple(this, baseTriple, componentIdx);
    }

    /** Create a field over a set of triples with a certain source and predicate.
     * Setting a value corresponds to a replacement of the intensional set of triples with a specific new one.
     *
     */
//    public ObservableValue<Node> createFieldForPredicate(Node source, Node predicate, boolean isForward) {
//        return createFieldForPredicate(source, predicate, isForward, null);
//    }

    public ObservableValue<Node> createValueField(Node sourceNode, DirectedFilteredTriplePattern dftp) {
        ObservableCollection<Node> set = createSetField(sourceNode, dftp);
        ObservableValue<Node> result = ObservableValueFromObservableCollection.decorate(set);
        return result;
    }

    public RdfField createSetField(Node sourceNode, Node predicate, boolean isForward) {
        DirectedFilteredTriplePattern dftp = DirectedFilteredTriplePattern.create(sourceNode, predicate, isForward);
        RdfField result = new RdfFieldForSubGraph(this, dftp);

        return result;
    }

    public ObservableCollection<Node> createSetField(Node sourceNode, DirectedFilteredTriplePattern dftp) {

        ObservableCollection<Node> set = SetOfNodesFromGraph.create(baseGraph, dftp);
        return set;
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


    public ObservableMap<Triple, Triple> getTripleReplacements() {
        return tripleReplacements;
    }

    public ObservableMap<Node, Node> getRenamedNodes() {
        return renamedNodes;
    }

    public ObservableGraph getBaseGraph() {
        return baseGraph;
    }

//    public Delta getDelta() {
//        Delta d = (Delta)((ObservableGraphImpl)delta).get();
//        return d;
//    }

//    public ObservableGraph getObservableDelta() {
//        return delta;
//    }

//
//    public ObservableGraph getDeletionGraph() {
//		return deletionGraph;
//	}

    public ObservableGraph getAdditionGraph() {
        return additionGraph;
    }

    public ObservableGraph getDeletionGraph() {
        return deletionGraph;
    }



    public static <T> Collection<T> nullableSingleton(T item) {
        return item == null
                ? Collections.emptySet()
                : Collections.singleton(item);
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

    /** Make the set of items contained in target equal to those contained in reference
     *  thereby calling .add() and .remove() only on items that differ.
     *  I.e. this method does NOT perform target.clear() followed by target.addAll(reference)
     *  */
    public static <T> void makeSetEqual(Set<T> target, Set<T> reference) {
        Set<T> deletions = new HashSet<>(Sets.difference(target, reference));
        Set<T> additions = new HashSet<>(Sets.difference(reference, target));

        target.removeAll(deletions);
        target.addAll(additions);
    }

    public static <T> Collection<T> defaultToSingletonIfEmpty(Collection<T> items, T defaultItem) {
        return items.isEmpty()
                ? Collections.singleton(defaultItem)
                : items;
    }

//    public static <T> Collection<T> defaultToSingletonIfEmpty(Collection<T> items, T defaultItem) {
//        return Sets.union(Collections.singleton(defaultItem), SetUtils.asSet(items));
//    }

    /**
     * Filter a collection based on a pattern with the following rules:
     * If pattern is null returned the collection unchanged.
     * Otherwise, if the pattern is contained in the collection return a collection with only that item
     * otherwise return an empty collection.
     *
     * @param <T>
     * @param collection
     * @param pattern
     * @return
     */
    public static <T> Collection<T> filterToPattern(Collection<T> collection, T pattern) {
        return pattern == null
                ? collection
                : collection.contains(pattern)
                    ? Collections.singleton(pattern)
                    : Collections.emptySet();
    }

    public static Collection<Node> filterToPattern(Function<Node, Collection<Node>> fn, Node node, Node pattern) {
        boolean isAnyNode = node == null || Node.ANY.equals(node);
        boolean isAnyPattern = pattern == null || Node.ANY.equals(pattern);

        Collection<Node> result;
        if (isAnyNode) {
            result = isAnyPattern
                    ? Collections.singleton(Node.ANY)
                    : Collections.singleton(pattern);
        } else {
            result = fn.apply(node);

            if (!isAnyPattern) {
                result = result.contains(pattern)
                    ? Collections.singleton(pattern)
                    : Collections.emptySet();
            }
        }

        return result;
    }


    public static <T> Collection<T> get(Multimap<T, T> multimap, T key, boolean reflexive) {
        Collection<T> result = multimap.get(key);

        result = reflexive
            ? result.contains(key)
                ? result
                : Sets.union(Collections.singleton(key), SetUtils.asSet(result))
            : result;

        return result;
    }

    public static <K, V> V getOrDefault(Function<? super K, ? extends V> fn, K key, V defaultValue) {
        V result = fn.apply(key);
        result = result == null ? defaultValue : result;
        return result;
    }

    /**
     * Create all possible triples by substituting each node in the triple
     * with all possible nodes w.r.t. the reverse mapping.
     * If a node is not mapped it remains itself.
     *
     * @param concrete
     * @param reverseMap
     * @return
     */
    public static Stream<Triple> expand(
            Triple concrete,
            Triple pattern,
//            Function<Node, Node> nodeToCluster,
            Function<Node, Collection<Node>> clusterToMembers
            ) {

//        Multimap<Node, Node> clusterToMembers = nodeToCluster.entrySet().stream()
//                .collect(Multimaps.toMultimap(Entry::getValue, Entry::getKey, HashMultimap::create));

        Node cs = concrete.getSubject();
        Node cp = concrete.getPredicate();
        Node co = concrete.getObject();

        CartesianProduct<Node> cart = CartesianProduct.create(
            filterToPattern(clusterToMembers, cs, pattern.getMatchSubject()),
            filterToPattern(clusterToMembers, cp, pattern.getMatchPredicate()),
            filterToPattern(clusterToMembers, co, pattern.getMatchObject())
        );


//        CartesianProduct<Node> cart = CartesianProduct.create(
//            filterToPattern(
////                defaultToSingletonIfEmpty(
//                        get(reverseMap, concrete.getSubject(), reflexive),
////                        concrete.getSubject()),
//                pattern.getMatchSubject()),
//            filterToPattern(
////                    defaultToSingletonIfEmpty(
//                            get(reverseMap, concrete.getPredicate(), reflexive),
////                            concrete.getPredicate()),
//                    pattern.getMatchPredicate()),
//            filterToPattern(
////                    defaultToSingletonIfEmpty(
//                            get(reverseMap, concrete.getObject(), reflexive),
////                            concrete.getObject())
//                    pattern.getMatchObject())
//        );

//        CartesianProduct<Node> cart = CartesianProduct.create(
//            filterToPattern(
//                defaultToSingletonIfEmpty(reverseMap.get(concrete.getSubject()), concrete.getSubject()),
//                pattern.getMatchSubject()),
//            filterToPattern(
//                defaultToSingletonIfEmpty(reverseMap.get(concrete.getPredicate()), concrete.getPredicate()),
//                pattern.getMatchPredicate()),
//            filterToPattern(
//                defaultToSingletonIfEmpty(reverseMap.get(concrete.getObject()), concrete.getObject()),
//                pattern.getMatchObject())
//        );

        Stream<Triple> result = cart.stream()
                .map(TripleUtils::listToTriple);

        List<Triple> tmp = result.collect(Collectors.toList());
        result = tmp.stream();

        return result;
    }


//    @Override
//    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
//        return null;
//    }
}
