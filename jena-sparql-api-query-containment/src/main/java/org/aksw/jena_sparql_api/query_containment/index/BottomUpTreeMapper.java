package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 *
 * @author raven
 *
 * @param <A> Node type of the first tree
 * @param <B> Node type of the other tree
 * @param <M> Type of the matching object
 * @param <C> Type of the matching contribution object
 * @param <V> The value of the mapping computation
 */
public class BottomUpTreeMapper<A, B, M, C, V> {

    protected Tree<A> viewTree;
    protected Tree<B> userTree;

    protected TriFunction<A, B, TreeMapping<A, B, M, V>, Entry<C, V>> nodeMapper;

    protected BiFunction<M, C, M> addMatchingContribution;
    protected Predicate<M> isMatchingUnsatisfiable;

    protected Supplier<Table<A, B, V>> tableSupplier;
    protected Function<Tree<A>, Stream<A>> bottomUpTraverser;


    public BottomUpTreeMapper(
            Tree<A> viewTree,
            Tree<B> userTree,
            TriFunction<A, B, TreeMapping<A, B, M, V>, Entry<C, V>> nodeMapper,
            BiFunction<M, C, M> addMatchingContribution,
            Predicate<M> isMatchingUnsatisfiable,
            Supplier<Table<A, B, V>> tableSupplier
            ) {
            //Function<Tree<A>, Stream<A>> bottomUpTraverser) {
        super();
        this.viewTree = viewTree;
        this.userTree = userTree;
        this.nodeMapper = nodeMapper;
        this.addMatchingContribution = addMatchingContribution;
        this.isMatchingUnsatisfiable = isMatchingUnsatisfiable;
        this.tableSupplier = tableSupplier;
        //this.bottomUpTraverser = bottomUpTraverser;
        this.bottomUpTraverser = BottomUpTreeTraversals::postOrder;
    }


    /**
     * Assumes that there is a 1:1 correspondence among the parents of any aligned pair of nodes
     *
     * @param baseSolution
     * @param leafAlignment
     * @return
     */
    public TreeMapping<A, B, M, V> solve(M baseSolution, Map<A, B> leafAlignment) {
        Table<A, B, V> nodeMapping = tableSupplier.get();//HashBasedTable.create();
        //tableSupplier.get();
        TreeMapping<A, B, M, V> result = new TreeMapping<>(viewTree, userTree, baseSolution, nodeMapping);

        Iterator<A> it = bottomUpTraverser.apply(viewTree).iterator();

        while(it.hasNext()) {
            A a = it.next();
            //Entry<B, M> bEntry = leafAlignment.row(a).entrySet().iterator().next();
            B b = leafAlignment.get(a);//bEntry.getKey();

            Entry<C, V> mappingEntry = nodeMapper.apply(a, b, result);
            V mapping = mappingEntry.getValue();

            // null means unsatisfiable
            if(mapping != null) {
                C contribution = mappingEntry.getKey();
                M newBaseSolution = addMatchingContribution.apply(baseSolution, contribution);

                boolean isAcceptable = !isMatchingUnsatisfiable.test(newBaseSolution);
                if(!isAcceptable) {
                    result = null;
                    break;
                }


                result.overallMatching = newBaseSolution;
                result.nodeMappings.put(a, b, mapping);
            }
        }

        return result;
    }
}