package org.aksw.jena_sparql_api.cache.tests;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concept_cache.trash.OpVisitorViewCacheApplier;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Multimap;




/**
 *
 *
 * @author raven
 *
 */
public class QfpcMap {
    protected FeatureMap<Expr, Void> clauseSigTo;



    public void add(Op cacheOp) {
    }


    /**
     * Performs a lookup with a qfpc
     * and returns
     *
     * @param queryQfpc
     * @return
     */
    public Map<Object, Object> get(QuadFilterPatternCanonical queryQfpc) {

        Op rawOp = null;
        rawOp = Algebra.toQuadForm(rawOp);

        // TODO We could create a mapping from (op) -> (op with replaced constants)
        // rawOp = ReplaceConstants.replace(rawOp);
        Generator<Var> generator = OpUtils.freshVars(rawOp);

        // Extract all quad filter patterns from the op
        Map<Op, ProjectedQuadFilterPattern> tmpCacheableOps = OpVisitorViewCacheApplier.detectPrimitiveCachableOps(rawOp);

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
                    QuadFilterPatternCanonical r = AlgebraUtils.canonicalize2(qfp, generator);
                    return r;
                }));

        Collection<QuadFilterPatternCanonical> qfpcs = qfpToCanonical.values();

        //qfpcs.stream().
        for(QuadFilterPatternCanonical qfpc : qfpcs) {
            FeatureMap<Expr, Multimap<Expr, Expr>> clausesIndex = AlgebraUtils.indexDnf(qfpc.getFilterDnf());

            for(Entry<Set<Expr>, Collection<Multimap<Expr, Expr>>> clauseSigToLiteralSigToLiteral : clausesIndex.entrySet()) {

            }
        }

        return null;
    }

}
