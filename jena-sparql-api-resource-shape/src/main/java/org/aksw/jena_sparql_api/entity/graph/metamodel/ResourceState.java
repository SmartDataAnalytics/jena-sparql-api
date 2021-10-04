package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;


// TODO The values should be kept in a Slice container which allows retrieval of ranges
// Slice is similar to list but data may be locally or remote

public class ResourceState {
    protected Node src;

    // Immediate properties
    protected Map<Node, Map<Node, Triple>> fwdStore;
    protected Map<Node, Map<Node, Triple>> bwdStore;

    // TODO Implement
    // protected Map<Path, Map<Node, Triple>> pathStore;


    boolean isFwdComplete;
    boolean isBwdComplete;

    public ResourceState(Node src) {
        this(src, new HashMap<>(), new HashMap<>(), false, false);
    }

    public ResourceState(
            Node src,
            Map<Node, Map<Node, Triple>> fwdStore,
            Map<Node, Map<Node, Triple>> bwdStore,
            boolean isFwdComplete, boolean isBwdComplete) {
        super();
        this.src = src;
        this.fwdStore = fwdStore;
        this.bwdStore = bwdStore;
        this.isFwdComplete = isFwdComplete;
        this.isBwdComplete = isBwdComplete;
    }


    public void add(boolean isFwd, Node p, Node o) {

        Triple t = TripleUtils.create(src, p, o, isFwd);

        if (isFwd) {
            fwdStore
                .computeIfAbsent(p, pp -> new HashMap<>())
                .computeIfAbsent(o, oo -> t);
        } else {
            bwdStore
                .computeIfAbsent(p, pp -> new HashMap<>())
                .computeIfAbsent(o, oo -> t);
        }
    }

    /**
     * Declare a predicate to be seen.
     * This creates an entry for it with an empty set of values.
     * The set of values can be updated later.
     *
     */
    public void declarePredicateSeen(boolean isFwd, Node p) {
        if (isFwd) {
            fwdStore.computeIfAbsent(p, pp -> new HashMap<>());
        } else {
            bwdStore.computeIfAbsent(p, pp -> new HashMap<>());
        }
    }

//    public boolean deletePredicate(boolean isFwd, Node p) {
//        boolean result = fwdStore.remove(p) != null;
//        return result;
//    }

    public void delete(boolean isFwd, Node p, Node o) {
        if (isFwd) {
            Map<Node, Triple> om = fwdStore.get(p);
            om.remove(o); // Do not remove an empty set in order to tread predicate as seen
//            if (om.remove(o) != null) { // If something was removed
//                if (om.isEmpty()) {
//                    fwdStore.remove(p);
//                }
//            }
        } else {
            Map<Node, Triple> om = bwdStore.get(p);
            om.remove(o);
//            if (om.remove(o) != null) { // If something was removed
//                if (om.isEmpty()) {
//                    bwdStore.remove(p);
//                }
//            }
        }
    }

    public Set<Node> getTargets(boolean isFwd, Node p) {
        Set<Node> result = isFwd
                ? Optional.ofNullable(fwdStore.get(p)).map(Map::keySet).orElse(null)
                : Optional.ofNullable(bwdStore.get(p)).map(Map::keySet).orElse(null);

        return result;
    }


    public Stream<Triple> streamCachedTriples() {
        return Stream.concat(
                fwdStore.entrySet().stream().flatMap(map -> map.getValue().values().stream()),
                bwdStore.entrySet().stream().flatMap(map -> map.getValue().values().stream()));

    }


    public Stream<TriplePath> find(Path path, Node o) {
//    	TriplePath tp;
//    	tp.getPath()
        return null;
    }

    /**
     * Yield triples matching the predicate and object patterns
     *
     * Returns null if Node.ANY is requested for p but the index is not marked as complete.
     * In other words, this method refuses to yield partial results.
     *
     *
     * @param isFwd
     * @param p
     * @param o
     * @return
     */
    public Stream<Triple> find(boolean isFwd, Node p, Node o) {
        Triple t = TripleUtils.create(src, p, o, isFwd);

        Stream<Triple> result;
        if (isFwd) {
            result = NodeUtils.isNullOrAny(p)
                    ? (isFwdComplete ? findO(o, fwdStore.values().stream()) : null)
                    : findO(o, Stream.ofNullable(fwdStore.get(p)));
        } else {
            result = NodeUtils.isNullOrAny(p)
                    ? (isBwdComplete ? findO(o, bwdStore.values().stream()) : null)
                    : findO(o, Stream.ofNullable(bwdStore.get(p)));
        }

        return result;
    }

    protected Stream<Triple> findO(Node o, Stream<Map<Node, Triple>> stream) {
        return NodeUtils.isNullOrAny(o)
            ? stream.flatMap(map -> map.values().stream())
            : stream.flatMap(map -> Stream.ofNullable(map.get(o)));
    }

    public Set<Node> getSeenPredicates(boolean isFwd) {
        Set<Node> result = isFwd
                ? fwdStore.keySet()
                : bwdStore.keySet();
        return result;
    }



    // Should we clean up any empty sets?
    public void setFwdComplete(boolean yesOrNo) {
        this.isFwdComplete = yesOrNo;
    }

    public void setBwdComplete(boolean yesOrNo) {
        this.isBwdComplete = yesOrNo;
    }


    /**
     * Returns the set of items (possibly empty) if known.
     * A result of null indicates that the cache does not that set.
     *
     * @param path
     * @return
     */
    public Set<Node> getFromCache(Path path) {
        Set<Node> result;

        if (path instanceof P_Path0) {
            P_Path0 p0 = (P_Path0)path;

            boolean isFwd = p0.isForward();
            Node p = p0.getNode();

            result = getTargets(isFwd, p);
        } else {
            return null;
        }

        return result;
    }

}
