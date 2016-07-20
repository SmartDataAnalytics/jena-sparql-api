package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.Predicate;
import org.aksw.jena_sparql_api.concept_cache.dirty.Tree;

public class TreeUtils {

    /**
     * Find the first ancestor for which the predicate evaluates to true
     * @param tree
     * @param node
     * @param predicate
     * 
     * @return
     */
    public static <T> T findAncestor(Tree<T> tree, T node, java.util.function.Predicate<T> predicate) {
        T current = node;
        do {
            current = tree.getParent(current);
        } while(!predicate.test(current));

        return current;
    }

    
    /**
     * In-order-search starting from the given node and descending into the tree.
     * Each node may be mapped to a value.
     * A predicate determines whether to stop descending further into a sub-tree.
     * Useful for extracting patterns from a tree.
     *
     * @param node
     * @param parentToChildren
     * @param nodeToValue
     * @param doDescend
     * @return
     */
    public static <T, V> Stream<Entry<T, V>> inOrderSearch(
            T node,
            Function<T, ? extends Iterable<T>> parentToChildren,
            Function<T, V> nodeToValue,
            BiPredicate<T, V> doDescend
            ) {

        V value = nodeToValue.apply(node);
        Entry<T, V> e = new SimpleEntry<>(node, value);
        boolean descend = doDescend.test(node, value);

        Stream<Entry<T, V>> result = Stream.of(e);
        if(descend) {
            Iterable<T> children = parentToChildren.apply(node);
            Stream<T> childStream = StreamSupport.stream(children.spliterator(), false);

            result = Stream.concat(
                    result,
                    childStream.flatMap(c -> inOrderSearch(
                            c,
                            parentToChildren,
                            nodeToValue,
                            doDescend)));
        }

        return result;
    }

    public static <T> List<T> getLeafs(Tree<T> tree) {
        List<T> result = new ArrayList<T>();
        T root = tree.getRoot();
        getLeafs(result, tree, root);
        return result;
    }

    public static <T> void getLeafs(Collection<T> result, Tree<T> tree, T node) {
        Collection<T> children = tree.getChildren(node);
        if(children.isEmpty()) {
            result.add(node);
        } else {
            for(T child : children) {
                getLeafs(result, tree, child);
            }
        }
    }

    /**
     * Traverse an op structure and create a map from each subOp to its immediate parent
     *
     * NOTE It must be ensured that common sub expressions are different objects,
     * since we are using an identity hash map for mapping children to parents
     *
     *
     * @param op
     * @return
     */
    public static <T> Map<T, T> parentMap(T root, Function<T, List<T>> parentToChildren) {
        Map<T, T> result = new IdentityHashMap<T, T>();

        result.put(root, null);

        parentMap(result, root, parentToChildren);
        return result;
    }

    public static <T> void parentMap(Map<T, T> result, T parent, Function<T, List<T>> parentToChildren) {
        List<T> children = parentToChildren.apply(parent);

        for(T child : children) {
            result.put(child, parent);

            parentMap(result, child, parentToChildren);
        }
    }
}
