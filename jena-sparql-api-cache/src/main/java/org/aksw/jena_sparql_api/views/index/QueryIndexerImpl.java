package org.aksw.jena_sparql_api.views.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeNode;
import org.aksw.commons.collections.trees.TreeNodeImpl;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMapImpl;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Multimap;

public class QueryIndexerImpl
    implements Function<Op, QueryIndex>
{
    public static Stream<Entry<Set<Expr>, QuadPatternIndex>> createQuadPatternIndex(Tree<Op> treeOp, Op qfpOp, QuadFilterPattern qfp) {
        TreeNode<Op> opRef = new TreeNodeImpl<>(treeOp, qfpOp);
        
        Generator<Var> generator = VarGeneratorImpl2.create();
        QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(qfp, generator);
        
        Set<Set<Expr>> dnf = qfpc.getFilterDnf();
        
        FeatureMap<Expr, Multimap<Expr, Expr>> dnfIndex = SparqlCacheUtils.indexDnf(dnf);
        
        //FeatureMap<Expr, QuadPatternIndex> result = new FeatureMapImpl<>();
        
//        dnfIndex.forEach((clauseFeatureSet, groupedClause) -> {
//            QuadPatternIndex quadPatternIndex = new QuadPatternIndex(groupedClause, opRef, qfpc);
//            
//            result.put(clauseFeatureSet, quadPatternIndex);
//        });
        Stream<Entry<Set<Expr>, QuadPatternIndex>> result = dnfIndex.entrySet().stream()
            .flatMap(e -> {
                Set<Expr> clauseFeatureSet = e.getKey();
                Collection<Multimap<Expr, Expr>> groupedClauses = e.getValue();
                
                Stream<Entry<Set<Expr>, QuadPatternIndex>> r = groupedClauses.stream()
                        .map(groupedClause -> new SimpleEntry<>(clauseFeatureSet, new QuadPatternIndex(groupedClause, opRef, qfpc)));
                
                return r;
            });
        
        return result;
    }

    public static Stream<Entry<Op, QuadFilterPattern>> streamQuadFilterPatterns(Op op) {
    	Tree<Op> tree = OpUtils.createTree(op);
    	Stream<Entry<Op, QuadFilterPattern>> result = streamQuadFilterPatterns(tree);
    	return result;
    }
    
    public static Stream<Entry<Op, QuadFilterPattern>> streamQuadFilterPatterns(Tree<Op> tree) {
        Stream<Entry<Op, QuadFilterPattern>> result = TreeUtils
                .inOrderSearch(
                        tree.getRoot(),
                        tree::getChildren,
                        SparqlCacheUtils::extractQuadFilterPattern,
                        (opNode, value) -> value == null) // descend while the value is null
                .filter(e -> e.getValue() != null);

        return result;
    }
    
    @Override
    public QueryIndex apply(Op op) {
        
        // Feature extractor for canonical quad filter pattern
        Function<QuadFilterPatternCanonical, Stream<Set<Object>>> qfpcFeatureExtractor;
        
        
        // Set up the cache feature set based on its quad filter pattern 
        
        Tree<Op> treeOp = OpUtils.createTree(op);
        
        // Traverse the cache op and create a mapping from op to QuadFilterPatternCanonical
        Map<Op, QuadFilterPattern> opToQfp = streamQuadFilterPatterns(op)
        		.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        FeatureMap<Expr, QuadPatternIndex> quadPatternIndex = new FeatureMapImpl<>();

        opToQfp.forEach((xop, qfp) -> {
            createQuadPatternIndex(treeOp, xop, qfp).forEach(e -> {
                quadPatternIndex.put(e.getKey(), e.getValue());
            });
        });

        Tree<Op> tree = OpUtils.createTree(op);
        
        
        QueryIndex result = new QueryIndex(op, tree, quadPatternIndex);
        
        return result;
    }
}
