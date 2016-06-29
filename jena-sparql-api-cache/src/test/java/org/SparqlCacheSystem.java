package org;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.concept_cache.op.TreeUtils;
import org.aksw.jena_sparql_api.concept_cache.trash.OpVisitorViewCacheApplier;
import org.aksw.jena_sparql_api.utils.Generator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

public class SparqlCacheSystem<D> {

    protected IndexSystem<Op, Op, D, String> indexSystem;

    public void registerCache(String name, Op cacheOp, D cacheData) {

        // Traverse the cache op and create a mapping from op to QuadFilterPatternCanonical
        Map<Op, QuadFilterPattern> opToQfp = TreeUtils
            .inOrderSearch(
                    cacheOp,
                    OpUtils::getSubOps,
                    SparqlCacheUtils::extractQuadFilterPattern,
                    (op, value) -> value == null) // descend while the value is null
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));



        // TODO We could create a mapping from (op) -> (op with replaced constants)
        // rawOp = ReplaceConstants.replace(rawOp);
        Generator<Var> generator = OpUtils.freshVars(cacheOp);

        // Determine which parts of the query are cacheable
        // (i.e. those parts that correspond to projected quad filter patterns)
        Map<Op, ProjectedQuadFilterPattern> tmpCacheableOps = OpVisitorViewCacheApplier.detectPrimitiveCachableOps(cacheOp);



        // If the op is a projection, associate the pqfp with the sub op in order to retain the projection
        // TODO This is necessary, if we later expand the graph pattern; yet, I am not sure this is the best way to retain the projection
        Map<Op, ProjectedQuadFilterPattern> cacheableOps = tmpCacheableOps.entrySet().stream()
            .collect(Collectors.toMap(e -> {
                Op op = e.getKey();
                Op r = op instanceof OpProject ? ((OpProject)op).getSubOp() : op;
                return r;
            }, Entry::getValue));


        Map<QuadFilterPattern, QuadFilterPatternCanonical> qfpToCanonical = cacheableOps.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getValue().getQuadFilterPattern(), e -> {
                ProjectedQuadFilterPattern pqfp = e.getValue();
                QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
                QuadFilterPatternCanonical r = SparqlCacheUtils.canonicalize2(qfp, generator);
                return r;
            }));


        // Perform BGP-feature based indexing
//        Tree<Op> cacheTree = OpUtils.createTree(cacheOp);
//
//        // Get all leaf nodes of the tree
//        List<Op> rawLeafs = TreeUtils.getLeafs(cacheTree);
//
//        // Map them to TreeNodes, so that each leaf is associated with the tree object
//        List<TreeNode<Op>> leafs = rawLeafs.stream()
//                .map(l -> new TreeNodeImpl<Op>(cacheTree, l))
//                .collect(Collectors.toList());
//
        // Extract features from the leaf


        // This is the op level indexing of cache
        indexSystem.put(cacheOp, cacheData);
    }


    public void rewriteQuery(Op queryOp) {
        Set<Entry<Op, T>> candidates = indexSystem.lookup(queryOp);



    }
}
