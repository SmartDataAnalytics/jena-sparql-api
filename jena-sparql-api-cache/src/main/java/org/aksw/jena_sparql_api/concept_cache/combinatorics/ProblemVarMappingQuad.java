package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.isomorphism.Problem;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.MapUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Sets;


public class ProblemVarMappingQuad
    extends ProblemMappingEquivBase<Quad, Quad, Var, Var>
{
    protected Set<Set<Expr>> aCnf;
    protected Set<Set<Expr>> bCnf;

    // The canonical cnf common to both sets of quads (essentially this is the key by which the quads were grouped)
    // This is obtained by remapping all variables of aCnf and bCnf with the same specific constant
    protected Set<Set<Expr>> canonicalCnf;

    /**
     * The constraints that apply to the given quads
     */
    protected Set<Set<Expr>> cnf;

    public ProblemVarMappingQuad(Collection<? extends Quad> as, Collection<? extends Quad> bs, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
        throw new RuntimeException("Should not be used anymore");
    }

    public ProblemVarMappingQuad(Collection<? extends Quad> as, Set<Set<Expr>> aCnf, Collection<? extends Quad> bs, Set<Set<Expr>> bCnf, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
        this.aCnf = aCnf;
        this.bCnf = bCnf;
    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions() {
        Iterable<Map<Var, Var>> tmp = CombinatoricsUtils.createSolutions(as, bs, baseSolution);
        Stream<Map<Var, Var>> result = StreamSupport.stream(tmp.spliterator(), false);
        return result;
    }

    @Override
    public Collection<Problem<Map<Var, Var>>> refine(Map<Var, Var> partialSolution) {
        Map<Var, Var> map = MapUtils.mergeIfCompatible(baseSolution, partialSolution);

        Collection<Problem<Map<Var, Var>>> result;
        if(map == null) {
            result = Collections.emptySet();
        } else {
            // TODO: Split the quads into equivalent classes

// From SparqlViewCache
//            public CacheResult lookup(QuadFilterPatternCanonical queryQfpc) { //PatternSummary queryPs) {
//                List<QfpcMatch> result = new ArrayList<QfpcMatch>();
//
//                // TODO: We need the quadToCnf map for the queryPs
//                IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(queryQfpc);

            result = null;
        }

//         = map == null
//                ? Collections.emptySet()
//                : Collections.singleton(new ProblemVarMappingQuad(as, bs, map))
//                ;

        return result;
    }

    public static QuadFilterPatternCanonical refine(QuadFilterPatternCanonical qfpc, Map<Var, Var> varMap) {
//        QuadFilterPatternCanonical aQfpc = new QuadFilterPatternCanonical(as, aCnf);
//        QuadFilterPatternCanonical bQfpc = new QuadFilterPatternCanonical(bs, bCnf);
        QuadFilterPatternCanonical aQfpc = new QuadFilterPatternCanonical(null, null);
        QuadFilterPatternCanonical bQfpc = new QuadFilterPatternCanonical(null, null);

        QuadFilterPatternCanonical aNewQfpc = QuadFilterPatternCanonical.applyVarMapping(aQfpc, varMap);
        QuadFilterPatternCanonical bNewQfpc = QuadFilterPatternCanonical.applyVarMapping(bQfpc, varMap);

        IBiSetMultimap<Quad, Set<Set<Expr>>> aQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(aNewQfpc);
        IBiSetMultimap<Quad, Set<Set<Expr>>> bQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(bNewQfpc);

        IBiSetMultimap<Set<Set<Expr>>, Quad> aCnfToQuad = aQuadToCnf.getInverse();
        IBiSetMultimap<Set<Set<Expr>>, Quad> bCnfToQuad = aQuadToCnf.getInverse();

//        Set<Set<Set<Expr>>> keys = Sets.union(aCnfToQuad.keySet(), bCnfToQuad.keySet());
//        for()

        Map<Set<Set<Expr>>, Entry<Set<Quad>, Set<Quad>>> quadGroups = groupByKey(aCnfToQuad, bCnfToQuad);

        //quadGroups.entrySet().stream()



        //NodeTransform nodeTransform = new Node
        return null;
    }

    public static <K, V> Map<K, Entry<Set<V>, Set<V>>> groupByKey(IBiSetMultimap<K, V> a, IBiSetMultimap<K, V> b) {
        Map<K, Entry<Set<V>, Set<V>>> result = new HashMap<>();

        Set<K> keys = Sets.union(a.keySet(), b.keySet());
        keys.forEach(
                k -> {
                    Set<V> av = SetUtils.asSet(a.get(k));
                    Set<V> bv = SetUtils.asSet(b.get(k));

                    Entry<Set<V>, Set<V>> e = new SimpleEntry<>(av, bv);
                    result.put(k, e);
                }
            );

        return result;
    }

}
