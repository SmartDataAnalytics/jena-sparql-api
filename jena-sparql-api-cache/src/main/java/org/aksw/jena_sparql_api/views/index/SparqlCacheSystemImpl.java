package org.aksw.jena_sparql_api.views.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemContainerNeighbourhoodAware;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.concept_cache.dirty.Tree;
import org.aksw.jena_sparql_api.concept_cache.dirty.TreeImpl;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

public class SparqlCacheSystemImpl {

    protected IndexSystem<Entry<Op, QueryIndex>, Op> indexSystem;
    protected Function<Op, QueryIndex> queryIndexer;
    //protected Map<Op, D> opToCacheData;

    public SparqlCacheSystemImpl() {
        indexSystem = IndexSystemImpl.create();
        queryIndexer = new QueryIndexerImpl();
    }

    public void registerCache(String name, Op cacheOp) { //, D cacheData) {
        QueryIndex queryIndex = queryIndexer.apply(cacheOp);

        // This is the op level indexing of cache
        indexSystem.add(new SimpleEntry<>(cacheOp, queryIndex));
        //opToCacheData.put(cacheOp, cacheData);
    }


    public void rewriteQuery(Op queryOp) {
        QueryIndex queryIndex = queryIndexer.apply(queryOp);

        // Create the initial set of cache candidates based on the query's algebra
        Collection<Entry<Op, QueryIndex>> candidates = indexSystem.lookup(queryOp);

        for(Entry<Op, QueryIndex> e : candidates) {
            QueryIndex cacheIndex = e.getValue();
            FeatureMap<Expr, QuadPatternIndex> cacheQpi = cacheIndex.getQuadPatternIndex();

            for(Entry<Set<Expr>, Collection<QuadPatternIndex>> f : queryIndex.getQuadPatternIndex().entrySet()) {
                Set<Expr> queryFeatureSet = f.getKey();
                Collection<QuadPatternIndex> queryQps = f.getValue();

                //Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSupersetOf(queryFeatureSet);
                Collection<Entry<Set<Expr>, QuadPatternIndex>> cacheQpiCandidates = cacheQpi.getIfSubsetOf(queryFeatureSet);

                for(QuadPatternIndex queryQp : queryQps) {
                    for(Entry<Set<Expr>, QuadPatternIndex> g : cacheQpiCandidates) {
                        QuadPatternIndex cacheQp = g.getValue();

                        System.out.println("CacheQP: " + cacheQp);
                        System.out.println("QueryQP: " + queryQp);

                        generateVarMappings(cacheQp, queryQp)
                            .forEach(x -> {
                                System.out.println("solution: " + x);
                                //cacheQp.getGroupedConjunction()

                            });
                        System.out.println("-----");

                    }
                }



            }


        }
    }


    public static Stream<Map<Var, Var>> generateVarMappings(QuadPatternIndex cache, QuadPatternIndex query) {
        Multimap<Expr, Expr> cacheMap = cache.getGroupedConjunction();
        Multimap<Expr, Expr> queryMap = query.getGroupedConjunction();

        Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(cacheMap.asMap(), queryMap.asMap());

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();

        group.values().stream()
            .map(x -> {
                Set<Expr> cacheExprs = x.getKey();
                Set<Expr> queryExprs = x.getValue();
                ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());

                //System.out.println("cacheExprs: " + cacheExprs);
                //System.out.println("queryExprs: " + queryExprs);

                //Stream<Map<Var, Var>> r = p.generateSolutions();

                return p;
            })
            .forEach(problems::add);

        ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(cache.getQfpc().getQuads(), query.getQfpc().getQuads(), Collections.emptyMap());
        problems.add(quadProblem);

//        for(int i = 0; i < 1000; ++i) {
//            Stopwatch sw = Stopwatch.createStarted();

        Stream<Map<Var, Var>> result = ProblemContainerNeighbourhoodAware.solve(
                problems,
                Collections.emptyMap(),
                Map::keySet,
                MapUtils::mergeIfCompatible,
                Objects::isNull);
//        }
        return result;
    }
    
    
    
    /**
     * Return a node's first ancestor having an arity > 1
     * null if there is none.
     * 
     * @param tree
     * @param node
     * @return
     */
    public static <T> T firstMultiaryAncestor(Tree<T> tree, T node) {
        T result = null;
        T current = node;
        while(current != null) {
            T parent = tree.getParent(result);
            List<T> children = tree.getChildren(parent);
            int arity = children.size();
            if(arity > 1) {
                result = parent;
                break;
            }
            current = parent;
        }
        return result;
    }

    public static <T> Tree<T> removeUnaryNodes(Tree<T> tree) {
        ListMultimap<T, T> parentToChildren = ArrayListMultimap.create();
        T newRoot = removeUnaryNodes(tree, tree.getRoot(), parentToChildren);
                
        Tree<T> result = newRoot == null
                ? null
                : TreeImpl.create(newRoot, (node) -> parentToChildren.get(node));
        
        return result;
    }

    public static <T> T removeUnaryNodes(Tree<T> tree, T node, ListMultimap<T, T> parentToChildren) {
        List<T> children = tree.getChildren(node);
        int childCount = children.size();
        
        T result;
        switch(childCount) {
        case 0:
            result = node;
            break;
        case 1:
            T child = children.get(0);
            result = removeUnaryNodes(tree, child, parentToChildren);
            break;
        default:
            result = node;
            for(T c : children) {
                T newChild = removeUnaryNodes(tree, c, parentToChildren);
                parentToChildren.put(node,  newChild);
            }
            break;
        }
                    
        return result;
    }

        
//    public static <T> Tree<T> removeUnaryNodes(Tree<T> tree) {
//
//        Predicate<T> isMultiary = (node) -> tree.getChildren(node).size() > 1;
//        
//        //Map<T, T> childToParent = new HashMap<>();
//        ListMultimap<T, T> parentToChildren = ArrayListMultimap.create();
//        
//        // for every leaf get the first non-unary parent
//        Collection<T> parents = TreeUtils.getLeafs(tree);
//        Collection<T> children = null;
//        while(!parents.isEmpty()) {
//            children = parents;
//
//            parents = new LinkedHashSet<T>();
//            for(T child : children) {
//                T parent = TreeUtils.findAncestor(tree, child, isMultiary);
//                if(parent != null) {
//                    parents.add(parent);
//                    parentToChildren.put(parent, child);
//                }
//            }        
//        }
//        
//        // There can be at most 1 root
//        T root = children.iterator().next(); //parents.isEmpty() ? null : parents.iterator().next(); 
//
//        
//        Tree<T> result = root == null
//                ? null
//                : TreeImpl.create(root, (node) -> parentToChildren.get(node));
//                
//        return result;
//    }


    // TODO: Another output format: Map<Entry<T, T>, Multimap<T, T>>
    /**
     * Input: A mapping from cache nodes to candidate query nodes represented as a Multimap<T, T>.
     * Output: The mapping partitioned by each node's first multiary ancestor.
     * 
     * Output could also be: Multimap<Op, Op> - fmaToNodesCache
     * 
     * 
     * For every cacheFma, map to the corresponding queryFmas - and for
     * each of these mappings yield the candidate node mappings of the children
     * Multimap<OpCacheFma, Map<OpQueryFma, Multimap<OpCache, OpQuery>>>
     * 
     * Q: What if cache nodes do not have a fma?
     * A: In this case the fma would be null, which means that there can only be a single cache node
     * which would be grouped with a null fma.
     * In the query, we can then check whether we are pairing a union with another union or null.
     * 
     * We always map from cache to query.
     * 
     * Map<CacheFma, QueryFma>
     * 
     * So the challenge is now again how to represent all the facts and how to perform
     * the permutations / combinations...
     * 
     * 
     * 
     * 
     * 
     * @param cacheTree
     * @param tree
     * @param cacheToQueryCands
     * @return
     */
    public static <T> Map<T, Multimap<T, T>> clusterNodesByFirstMultiaryAncestor(Tree<T> tree, Multimap<T, T> mapping) { //Collection<T> nodes) {
        Map<T, Multimap<T, T>> result = new HashMap<>();
        
        Set<Entry<T, Collection<T>>> entries = mapping.asMap().entrySet();
        for(Entry<T, Collection<T>> entry : entries) {
            T node = entry.getKey();
            //T multiaryAncestor = firstMultiaryAncestor(tree, cacheNode);
            T multiaryAncestor = tree.getParent(node);
            Collection<T> queryNodes = entry.getValue();
            
            for(T targetNode : queryNodes) {
                Multimap<T, T> mm = result.computeIfAbsent(multiaryAncestor, (k) -> HashMultimap.<T, T>create());
                mm.put(node, targetNode);
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param cacheParentToNodeMaps: Mapping from a cache parent, to the candidate op-mappings from cache op to query op. Example: { 5: { A: {1}, B: {1} } }
     * 
     */
    public static void matchOpTrees(Map<Op, Multimap<Op, Op>> cacheParentToNodeMaps, Tree<Op> cacheTree, Tree<Op> queryTree) {
        
    }
}
