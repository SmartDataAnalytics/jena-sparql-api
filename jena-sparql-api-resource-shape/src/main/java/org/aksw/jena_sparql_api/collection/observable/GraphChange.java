package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableMap;
import org.aksw.commons.collection.observable.ObservableMapImpl;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.commons.collection.observable.ObservableSetImpl;
import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.relation.DirectedFilteredTriplePattern;
import org.aksw.jena_sparql_api.util.tuple.TupleAccessorTriple;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class GraphChange
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
        this(ObservableMapImpl.decorate(new HashMap<>()), ObservableMapImpl.decorate(new HashMap<>()));
        //new Delta(GraphFactory.createPlainGraph()));
    }

    public GraphChange(ObservableMap<Node, Node> renamedNodes, ObservableMap<Triple, Triple> tripleReplacements) {
        super();
        this.renamedNodes = renamedNodes;
        this.tripleReplacements = tripleReplacements;

        this.additionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());
        this.deletionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());


        this.effectiveAdditionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());
        this.effectiveDeletionGraph = ObservableGraphImpl.decorate(GraphFactory.createDefaultGraph());


//        tripleReplacements.addPropertyChangeListener(ev -> refreshDeletions());
//        additionGraph.addPropertyChangeListener(ev -> refreshDeletions());
//        deletionGraph.addPropertyChangeListener(ev -> refreshDeletions());
//        renamedNodes.addPropertyChangeListener(ev -> refreshDeletions());
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


    public RdfField createSetField(Node sourceNode, Node predicate, boolean isForward) {
        DirectedFilteredTriplePattern dftp = DirectedFilteredTriplePattern.create(sourceNode, predicate, isForward);
        RdfField result = new RdfFieldForSubGraph(this, dftp);

        return result;
    }



    public ObservableMap<Triple, Triple> getTripleReplacements() {
        return tripleReplacements;
    }

    public ObservableMap<Node, Node> getRenamedNodes() {
        return renamedNodes;
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




    public static Table createTableFromEnties(
            Var keyVar,
            Var valueVar,
            Collection<? extends Entry<? extends Node, ? extends Node>> entrySet) {
        Table result = TableFactory.create(Arrays.asList(keyVar, valueVar));

        for (Entry<? extends Node, ? extends Node> e : entrySet) {
            Binding binding = BindingFactory.builder()
                    .add(keyVar, e.getKey())
                    .add(valueVar, e.getValue())
                    .build();

            result.addBinding(binding);
        }

        return result;
    }

    public static UpdateDeleteInsert createUpdateRenameComponent(
            int componentIdx,
            Collection<? extends Entry<? extends Node, ? extends Node>> renamedNodes
    ) {
        // INSERT { ?x ?p ?o } DELETE {?s ?p ?o } WHERE { SELECT DISTINCT ?s ?x { VALUES (?s ?x) { ... } ?s ?p ?o } }


        Node[] nodes = new Node[] { Vars.s, Vars.p, Vars.o };
        Var oldVar = (Var)nodes[componentIdx];
        Var newVar = Vars.x;

        Table renameTable = createTableFromEnties(oldVar, newVar, renamedNodes);
        Triple oldTp = TripleUtils.fromArray(nodes);

        nodes[componentIdx] = newVar;
        Triple newTp = TripleUtils.fromArray(nodes);

        Query subQuery = new Query();
        subQuery.setQuerySelectType();
        subQuery.setDistinct(true);
        subQuery.addResultVar(oldVar);
        subQuery.addResultVar(newVar);
        subQuery.setQueryPattern(ElementUtils.groupIfNeeded(
            new ElementData(renameTable.getVars(), Lists.newArrayList(renameTable.rows())),
            ElementUtils.createElement(oldTp)));

        UpdateDeleteInsert renameUpdate = new UpdateDeleteInsert();
        renameUpdate.getInsertAcc().addTriple(newTp);
        renameUpdate.getDeleteAcc().addTriple(oldTp);
        renameUpdate.setElement(new ElementSubQuery(subQuery));

        return renameUpdate;
    }


    /**
     * Creates a SPARQL update request from the state of this object as follows:
     *
     * <ol>
     *   <li>Pre-rename deletes: Delete the concrete triples (without renaming)</li>
     *   <li>Resource renaming: Rename subjects/predicate/objects</li>
     *   <li>Post-rename updates: Apply the triple replacements after renaming; unchanged components are subject to renaming / those that differ not</li>
     *   <li>Add the concrete triples</li>
     * </ol>
     *
     *
     * @return
     */
    public UpdateRequest toUpdateRequest() {

        UpdateRequest result = new UpdateRequest();

        // Delete triples marked for deletion
        if (!deletionGraph.isEmpty()) {
            QuadDataAcc acc = new QuadDataAcc();
            deletionGraph.find().forEach(acc::addTriple);
            result.add(new UpdateDataDelete(acc));
        }


        // Apply renaming to resources (skip renaming of newly added blank nodes)
        // TODO We also skip renaming of blank nodes - raise an exception?
        Map<Node, Node> effectiveRenames = Maps.filterEntries(this.renamedNodes, e -> !Objects.equals(e.getKey(), e.getValue()));
        Map<Node, Node> existingRenames = Maps.filterKeys(effectiveRenames, key -> !newNodes.contains(key) && !key.isBlank());

        if (!existingRenames.isEmpty()) {
            Update renameS = createUpdateRenameComponent(0, existingRenames.entrySet());
            Update renameP = createUpdateRenameComponent(1, existingRenames.entrySet());
            Update renameO = createUpdateRenameComponent(2, existingRenames.entrySet());

            result.add(renameS);
            result.add(renameP);
            result.add(renameO);
        }

        // UpdateDeleteInsert postRenameUpdates = new UpdateDeleteInsert();
        QuadDataAcc postRenameDeletes = new QuadDataAcc();
        QuadDataAcc postRenameInserts = new QuadDataAcc();


        Map<Triple, Triple> effectiveTripleReplacements = Maps.filterEntries(this.tripleReplacements, e -> !Objects.equals(e.getKey(), e.getValue()));
        NodeTransform nodeTransform = new NodeTransformRenameMap(effectiveRenames);

        if (!effectiveTripleReplacements.isEmpty()) {

            for (Entry<Triple, Triple> e : this.tripleReplacements.entrySet()) {
                // Apply renaming to all components that are unchanged
                Triple before = e.getKey();
                Triple after = e.getValue();

                Triple toDelete = NodeTransformLib.transform(nodeTransform, before);
                postRenameDeletes.addTriple(toDelete);

                Node[] nodes = new Node[3];
                for (int i = 0; i < 3; ++i) {
                    Node b = TupleAccessorTriple.getComponent(before, i);
                    Node a = TupleAccessorTriple.getComponent(after, i);
                    nodes[i] = Objects.equals(b, a)
                        ? effectiveRenames.getOrDefault(b, b)
                        : a;
                }
                Triple toAdd = TripleUtils.fromArray(nodes);
                postRenameInserts.addTriple(toAdd);
            }

        }

        additionGraph.find()
            .mapWith(t -> NodeTransformLib.transform(nodeTransform, t))
            .forEach(postRenameInserts::addTriple);

        if (!postRenameDeletes.getQuads().isEmpty()) {
            result.add(new UpdateDataDelete(postRenameDeletes));
        }

        if (!postRenameInserts.getQuads().isEmpty()) {
            result.add(new UpdateDataInsert(postRenameInserts));
        }

        return result;
    }
}
