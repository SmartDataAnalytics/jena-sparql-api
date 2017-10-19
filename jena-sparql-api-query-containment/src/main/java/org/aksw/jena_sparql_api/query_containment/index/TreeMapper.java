package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.combinatorics.solvers.collections.ProblemContainer;
import org.aksw.combinatorics.solvers.collections.ProblemContainerImpl;
import org.aksw.combinatorics.solvers.collections.ProblemSolver;
import org.aksw.commons.collections.multimaps.MultimapUtils;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

/**
 * 
 * 
 * 
 * TODO Depth-First iteration vs Per Layer iteration (bottom-up breadth first)
 *
 * @author raven
 *
 * @param <K>
 * @param <G>
 * @param <N>
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <M>
 */
public class TreeMapper<K, XA, XB, YA, YB, A, B, M, C, V> {

	private static final Logger logger = LoggerFactory.getLogger(TreeMapper.class);

	// Two functions: One maps a key to some object XA, the other extracts the tree from it
    protected Function<? super K, ? extends XA> viewKeyToViewContext;
    protected Function<? super XA, ? extends Tree<A>> viewContextToTree;
    
    
    //protected Function<?>

    protected Function<? super XB, ? extends Tree<B>> getTree;
    protected Function<? super XB, Map<B, YB>> getLeafNodes;
    
    protected TriFunction<XB, Entry<B, YB>, M, Table<K, A, ProblemNeighborhoodAware<M, ?>>> leafMatcher;

    
    //protected TriFunction<? super A, ? super B, TreeMapping<A, B, M, V>, ? extends Entry<C, V>> nodeMapper;
    //protected BiFunction<? super XA, ?super XB, ? extends NodeMapper<A, B, M, V>> nodeMapperFactory;
    protected BiFunction<? super XA, ?super XB, ? extends NodeMapper<A, B, M, C, V>> nodeMapperFactory;
    
    
    protected BiFunction<M, C, M> addMatchingContribution;
    protected BinaryOperator<M> matchingCombiner;
    protected Predicate<M> isMatchingUnsatisfiable;

    protected boolean aIdentity;
    protected boolean bIdentity;

    public TreeMapper(
    		Function<? super K, ? extends XA> viewKeyToXA,
    		Function<? super XA, ? extends Tree<A>> xaToTree,

    	    Function<? super XB, ? extends Tree<B>> getTree,
    		Function<? super XB, Map<B, YB>> getLeafNodes,
    				
            TriFunction<XB, Entry<B, YB>, M, Table<K, A, ProblemNeighborhoodAware<M, ?>>> leafMatcher,
            //TriFunction<? super A, ? super B, TreeMapping<A, B, M, V>, ? extends Entry<C, V>> nodeMapper,
            //BiFunction<? super XA, ?super XB, ? extends NodeMapper<A, B, M, V>> nodeMapperFactory,
            BiFunction<? super XA, ?super XB, ? extends NodeMapper<A, B, M, C, V>> nodeMapperFactory,
            
            BiFunction<M, C, M> addMatchingContribution,
            //Function<Tree<A>, Stream<A>> bottomUpTraverser,
            //Supplier<M> createEmptyMatching,
            BinaryOperator<M> matchingCombiner,
            Predicate<M> isMatchingUnsatisfiable,
            boolean aIdentity,
            boolean bIdentity) {
        super();
        this.viewKeyToViewContext = viewKeyToXA;
        this.viewContextToTree = xaToTree;
        
        this.getTree = getTree;
        this.getLeafNodes = getLeafNodes;
        
        
        this.leafMatcher = leafMatcher;
        this.nodeMapperFactory = nodeMapperFactory;
        this.addMatchingContribution = addMatchingContribution;
        //this.bottomUpTraverser = bottomUpTraverser;
        //this.createEmptyMatching = createEmptyMatching;
        this.matchingCombiner = matchingCombiner;
        this.isMatchingUnsatisfiable = isMatchingUnsatisfiable;
        this.aIdentity = aIdentity;
        this.bIdentity = bIdentity;
    }



    public static <R, C, V> Table<R, C, V> createTable(boolean rowIdentity, boolean columnIdentity) {
        Map<R, Map<C, V>> backingMap = createMap(rowIdentity);

        Supplier<Map<C, V>> supplier = columnIdentity
                ? IdentityHashMap::new
                : LinkedHashMap::new;

        Table<R, C, V> result = Tables.newCustomTable(backingMap, supplier::get);
        return result;
    }


    public static <K, V> Map<K, V> createMap(boolean useIdentity) {
        Map<K, V> result = useIdentity
                ? new IdentityHashMap<>()
                : new LinkedHashMap<>();
                
        return result;
    }

    public Stream<Entry<K, TreeMapping<A, B, M, V>>> createMappings(M baseMatching, XB userContext) {

        //Table<K, Entry<A, B>, Table<A, B, ProblemNeighborhoodAware<S, S>>> leafMappings = createLeafMappings(userTree);
        Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> leafMappingPerView = createLeafMappings(baseMatching, userContext);

        Tree<B> userTree = getTree.apply(userContext);

        Stream<Entry<K, TreeMapping<A, B, M, V>>> result = leafMappingPerView.entrySet().stream()
            .flatMap(e -> {
                K viewKey = e.getKey();
                //logger.debug("Processing view with key: " + viewKey);

                XA viewContext = viewKeyToViewContext.apply(viewKey);
                Tree<A> viewTree = viewContextToTree.apply(viewContext);//viewKeyToTree.apply(viewKey);

                //XB xb = null;
                //NodeMapper<A, B, M, V> 
                NodeMapper<A, B, M, C, V> nodeMapper = nodeMapperFactory.apply(viewContext, userContext);
                
                BottomUpTreeMapper<A, B, M, C, V> treeMapper = new BottomUpTreeMapper<A, B, M, C, V>(
                        viewTree,
                        userTree,
                        nodeMapper,
                        addMatchingContribution, isMatchingUnsatisfiable,
                        () -> createTable(aIdentity, bIdentity)
//                        BottomUpTreeTraversals::postOrder//bottomUpTraverser
                        );


                Table<A, B, ProblemNeighborhoodAware<M, ?>> alignmentProblems = e.getValue();
                Multimap<A, B> mm = MultimapUtils.newSetMultimap(aIdentity, bIdentity);

                for(Cell<A, B, ProblemNeighborhoodAware<M, ?>> cell : alignmentProblems.cellSet()) {
                    mm.put(cell.getRowKey(), cell.getColumnKey());
                }


                Multimap<A, A> viewAncestorToChildren = MultimapUtils.groupBy(mm.keys(), (node) -> TreeUtils.getFirstMultiaryAncestor(viewTree, node), MultimapUtils.newSetMultimap(aIdentity, aIdentity));
                //Multimap<B, B> userMultiaryAncestorToChildren = MultimapUtils.groupBy(mm.values(), (node) -> TreeUtils.getFirstMultiaryAncestor(userTree, node), MultimapUtils.newSetMultimap(bIdentity, bIdentity));

                Multimap<A, B> ancestorCandAlignment = MultimapUtils.newSetMultimap(aIdentity, bIdentity);
                for(Entry<A, A> f : viewAncestorToChildren.entries()) {
                    A aAncestor = f.getKey();
                    A aChild = f.getValue();

                    Collection<B> bChildren = alignmentProblems.row(aChild).keySet();
                    for(B b : bChildren) {
                        B bAncestor = TreeUtils.getFirstMultiaryAncestor(userTree, b);
                        ancestorCandAlignment.put(aAncestor, bAncestor);
                    }
                }


//                for(Entry<A, Collection<B>> x : mm.asMap().entrySet()) {
//                    A a = x.getKey();
//                    A aAncestor = TreeUtils.getFirstMultiaryAncestor(viewTree, a); //viewTree.getParent(a);
//                    for(B b : x.getValue()) {
//                        B bAncestor = TreeUtils.getFirstMultiaryAncestor(userTree, b);
//                        multiaryAncestorCandAlignment.put(aAncestor, bAncestor);
//                    }
//                }

                Stream<Map<A, B>> childAlignmentStream = KPermutationsOfNUtils.kPermutationsOfN(ancestorCandAlignment, aIdentity, bIdentity).flatMap(parentAlignment -> {
                    // For each parent alignment, create the kPermutationsOfN for the children
                    Multimap<A, B> childCandAlignment = MultimapUtils.newSetMultimap(aIdentity, bIdentity);
                    for(Entry<A, B> f : parentAlignment.entrySet()) {
                        A aMultiaryAncestor = f.getKey();

                        Collection<A> aChildren = viewAncestorToChildren.get(aMultiaryAncestor);

                        for(A aChild : aChildren) {
                            Set<B> bChildren = alignmentProblems.row(aChild).keySet();
                            for(B bChild : bChildren) {
                                B bAncestor = TreeUtils.getFirstMultiaryAncestor(userTree, bChild);
                                boolean isMatch = ancestorCandAlignment.containsEntry(aMultiaryAncestor, bAncestor);
                                if(isMatch) {
                                    childCandAlignment.put(aChild, bChild);
                                }
                            }
                        }

//                        B bParent = f.getValue();
//                        for(A aChild : viewMultiaryAncestorToChildren.get(aMultiaryAncestor)) {
//                            Collection<B> bChildren = userMultiaryAncestorToChildren.get(bParent);
//                            childCandAlignment.putAll(aChild, bChildren);
//                        }

                    }

                    // TODO Based on the parent alignment, we could use different strategies to match the children
                    // e.g. sequential
                    
                    
                    Stream<Map<A, B>> t = KPermutationsOfNUtils.kPermutationsOfN(childCandAlignment, aIdentity, bIdentity);

                    return t;
                });




                // TODO Filter out mappings of view nodes with same parent to user nodes with different parents
                //Stream<Map<A, B>> xxx = KPermutationsOfNUtils.kPermutationsOfN(mm, this::createMap);


                Stream<TreeMapping<A, B, M, V>> r = childAlignmentStream.flatMap(leafAlignment -> {
                    // Get all the problems
                    Collection<ProblemNeighborhoodAware<M, ?>> problems = leafAlignment.entrySet().stream()
                            .map(f -> alignmentProblems.get(f.getKey(), f.getValue()))
                            .collect(Collectors.toList());

                    //M baseMatching = createEmptyMatching.get();
//System.out.println("Got child alignment");
                    ProblemContainer<M> problemContainer = ProblemContainerImpl.create(problems);
                    ProblemSolver<M> solver = new ProblemSolver<>(problemContainer, baseMatching, matchingCombiner);

                    return solver.streamSolutions().map(matching -> {
                        // For each alignment and matching perform the tree mapping

                        TreeMapping<A, B, M, V> s = treeMapper.solve(matching, leafAlignment);

                        return s;
                    });
                });

                Stream<Entry<K, TreeMapping<A, B, M, V>>> t = r
                		.filter(item -> item != null)
                		.map(item -> new SimpleEntry<>(viewKey, item));
                return t;
            });

        return result;
    }



    // for each view key there may be multiple tree alignments
    // The concrete mapping: Multimap<K, Entry<S, Table<A, B, Entry<S, C>>>>
    //                                         ^ overall mapping so far


    /**
     *
     */
    public Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> createLeafMappings(M baseMatching, XB userContext) { //Tree<B> userTree) {

        //Collection<B> leafNodes = TreeUtils.getLeafs(userTree);
        Map<B, YB> leafMap = getLeafNodes.apply(userContext);
        
        Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> result = createLeafMappings(baseMatching, userContext, leafMap.entrySet());
        
        return result;
    }
    
    public Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> createLeafMappings(M baseMatching, XB userContext, Collection<Entry<B, YB>> leafEntries) { 
        //for(B userOp : leafNodes) {
        Map<K, Table<A, B, ProblemNeighborhoodAware<M, ?>>> result = new HashMap<>();
        for(Entry<B, YB> leafEntry : leafEntries) {

        	//Tree<B> userTree = getTree.apply(userContext);
        	B userOp = leafEntry.getKey();
        	//YB leafData = leafEntry.getValue();
        	
            // Create the initial matching for the leafs
            // Obtain the candidate views for that user node
            Table<K, A, ProblemNeighborhoodAware<M, ?>> matchTable = leafMatcher.apply(userContext, leafEntry, baseMatching);
            
            logger.debug("Found " + matchTable.rowMap().size() + " candidate leafs  for" + userOp);
            
            for(Entry<K, Map<A, ProblemNeighborhoodAware<M, ?>>> matchEntry : matchTable.rowMap().entrySet()) {

                K viewKey = matchEntry.getKey();

                Table<A, B, ProblemNeighborhoodAware<M, ?>> table = result.computeIfAbsent(viewKey,
                        (k) -> TreeMapper.createTable(aIdentity, bIdentity));

                for(Entry<A, ProblemNeighborhoodAware<M, ?>> leafMatching : matchEntry.getValue().entrySet()) {
                    A viewOp = leafMatching.getKey();
                    ProblemNeighborhoodAware<M, ?> matching = leafMatching.getValue();

                    table.put(viewOp, userOp, matching);
                }
            }
        }

        return result;
    }

}
