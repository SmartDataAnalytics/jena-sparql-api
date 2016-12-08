package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.Problem;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;


public class ProblemVarMappingQuad
    extends ProblemMappingVarsBase<Quad, Quad, Var, Var>
{
    /**
     * The constraints that apply to the given quads
     */
    public ProblemVarMappingQuad(Collection<Quad> as, Collection<Quad> bs, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions() {
        Stream<Map<Var, Var>> result = CombinatoricsUtils.createSolutions(as, bs, baseSolution);
        return result;
    }

    @Override
    public Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> refine(Map<Var, Var> partialSolution) {
        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = Refinement.refineQuads(
                as,
                bs,
                baseSolution,
                partialSolution);

        return result;
    }

    @Override
    public Collection<Var> getSourceNeighbourhood() {
        Set<Var> result = QuadPatternUtils.getVarsMentioned(as);
        return result;
    }

    
    
    
//    @Override
//    public Collection<Var> exposeTargetNeighbourhood() {
//        Set<Var> result = QuadPatternUtils.getVarsMentioned(bs);
//        return result;
//    }



//    public static Multimap<Set<Expr>, Set<Expr>> signatureToClauses(Iterable<? extends Iterable<Expr>> clauses) {
//        Multimap<Set<Expr>, Set<Expr>> result = HashMultimap.create();
//        for(Iterable<Expr> clause : clauses) {
//            Set<Expr> signature = ClauseUtils.signaturize(clause);
//            Set<Expr> c = SetUtils.asSet(clause);
//            result.put(signature, c);
//        }
//
//        return result;
//    }
//
//
//    /**
//     *
//     * { v1: (g, s, x, a),
//     *   v2: (g, s, y, b) }
//     *
//     *  fn(s, x, g)
//     *
//     * { (a -> x), (b -> y) }
//     *
//     * label(join_v1_v2(v1, v2)):
//     *
//     *
//     * every state can have a set of open problems and a flag of whether its parent may have open problems.
//     *
//     *
//     *
//     * @param quads
//     * @param varMap
//     */
//    public static Collection<Collection<Quad>> refine(Collection<Quad> quads, Map<Var, Var> varMap) {
//        return null;
//    }
//
//    public static QuadFilterPatternCanonical refine(QuadFilterPatternCanonical qfpc, Map<Var, Var> varMap) {
////        QuadFilterPatternCanonical aQfpc = new QuadFilterPatternCanonical(as, aCnf);
////        QuadFilterPatternCanonical bQfpc = new QuadFilterPatternCanonical(bs, bCnf);
//        QuadFilterPatternCanonical aQfpc = new QuadFilterPatternCanonical(null, null);
//        QuadFilterPatternCanonical bQfpc = new QuadFilterPatternCanonical(null, null);
//
//        QuadFilterPatternCanonical aNewQfpc = QuadFilterPatternCanonical.applyVarMapping(aQfpc, varMap);
//        QuadFilterPatternCanonical bNewQfpc = QuadFilterPatternCanonical.applyVarMapping(bQfpc, varMap);
//
//        IBiSetMultimap<Quad, Set<Set<Expr>>> aQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(aNewQfpc);
//        IBiSetMultimap<Quad, Set<Set<Expr>>> bQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(bNewQfpc);
//
//        IBiSetMultimap<Set<Set<Expr>>, Quad> aCnfToQuad = aQuadToCnf.getInverse();
//        IBiSetMultimap<Set<Set<Expr>>, Quad> bCnfToQuad = bQuadToCnf.getInverse();
//
////        Set<Set<Set<Expr>>> keys = Sets.union(aCnfToQuad.keySet(), bCnfToQuad.keySet());
////        for()
//
//        List<Problem<Map<Var, Var>>> problems = new ArrayList<>();
//        Map<Set<Set<Expr>>, Entry<Set<Quad>, Set<Quad>>> quadGroups = groupByKey(aCnfToQuad, bCnfToQuad);
//
//        for(Entry<Set<Quad>, Set<Quad>> quadGroup : quadGroups.values()) {
//            Set<Quad> as = quadGroup.getKey();
//            Set<Quad> bs = quadGroup.getValue();
//            Problem<Map<Var, Var>> x = new ProblemVarMappingQuad(as, bs, varMap);
//            problems.add(x);
//        }
//
//
//        Multimap<Set<Expr>, Set<Expr>> mapA = signatureToClauses(aQfpc.getFilterCnf());
//        Multimap<Set<Expr>, Set<Expr>> mapB = signatureToClauses(bQfpc.getFilterCnf());
//
//        Map<Set<Expr>, Entry<Set<Set<Expr>>, Set<Set<Expr>>>> exprGroups = groupByKey(mapA.asMap(), mapB.asMap());
//
//        for(Entry<Set<Set<Expr>>, Set<Set<Expr>>> entry : exprGroups.values()) {
//            Set<Set<Expr>> as = entry.getKey();
//            Set<Set<Expr>> bs = entry.getValue();
//            Problem<Map<Var, Var>> x = null; //new ProblemVarMappingExpr(as, bs, varMap);
//            problems.add(x);
//        }
//
//
//
//        // re-index the expressions
//        //SparqlCacheUtils.createM
//
//
//
//        //quadGroups.entrySet().stream()
//
//
//
//        //NodeTransform nodeTransform = new Node
//        return null;
//    }


//    public static <K, V> Map<K, Entry<Set<V>, Set<V>>> groupByKey(Multimap<K, V> a, Map<K, V> b) {
//        Map<K, Entry<V, V>> result = new HashMap<>();
//
//        Set<K> keys = Sets.union(a.keySet(), b.keySet());
//        keys.forEach(
//                k -> {
//                    Set<V> av = SetUtils.asSet(a.get(k));
//                    Set<V> bv = SetUtils.asSet(b.get(k));
//
//                    Entry<Set<V>, Set<V>> e = new SimpleEntry<>(av, bv);
//                    result.put(k, e);
//                }
//            );
//
//        return result;
//    }

}
