package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.apache.jena.ext.com.google.common.collect.Streams;

import com.google.common.collect.AbstractIterator;

// https://stackoverflow.com/questions/42780279/bottom-up-tree-traversal-starting-from-a-particular-leaf
/**
 * All traversals guarantee, that all reachable children of an inner node have been visited before that inner node is returned
 *
 * @author raven
 *
 */
public class BottomUpTreeTraversals {
    public static <T> Stream<T> postOrder(T node, Function<T, Collection<T>> getChildren) {
        return Stream.concat(
                getChildren.apply(node).stream().flatMap(child -> postOrder(child, getChildren)),
                Stream.of(node));
    }

    public static <T> Stream<T> postOrder(Tree<T> tree) {
        return postOrder(tree.getRoot(), tree::getChildren);
    }

    public static <T> Stream<T> leafsWithSameParentFirstThenParent(Tree<T> tree) {
        return null;
    }


    /**
     * this is post-order traversal but with layers
     *
     * 'covered' parents: parent, where all children have been traversed before
     *
     *
     */
    public static <T> Stream<Set<T>> allLeafsFirstThenCoveredParents(Tree<T> tree, Supplier<Set<T>> setSupplier) {
        Set<T> seen = setSupplier.get();

        Set<T> leafs = setSupplier.get();
        seen.addAll(leafs);


        Iterator<Set<T>> it = new AbstractIterator<Set<T>>() {
            Set<T> current = null;

            @Override
            protected Set<T> computeNext() {
                Set<T> result;

                if(current == null) {
                    result = setSupplier.get();
                    result.addAll(TreeUtils.getLeafs(tree));
                } else {
                    result = coveredParentsOf(tree, seen, current, setSupplier.get());
                    seen.addAll(result);
                }

                if(result.isEmpty()) {
                    result = endOfData();
                }

                return result;
            }

        };

        return Streams.stream(it);
    }

    public static <T> Set<T> coveredParentsOf(Tree<T> tree, Set<T> seen, Set<T> current, Set<T> result) {
        // Collect all parents of the 'current' set ...
        for(T node : current) {
            T parent = tree.getParent(node);
            result.add(parent);
        }

        // ... and only retain those where all children are in the 'seen' set.
        Iterator<T> it = result.iterator();
        while(it.hasNext()) {
            T parent = it.next();
            Collection<T> children = parent == null ? Collections.singleton(tree.getRoot()) : tree.getChildren(parent);

            if(!seen.containsAll(children)) {
                it.remove();
            }
        }

        return result;
    }


    /**
     *
     *
     */
    public static <T> Stream<T> deepLeafsFirstThenNextLeafs(Tree<T> tree) {
        return null;
    }
}