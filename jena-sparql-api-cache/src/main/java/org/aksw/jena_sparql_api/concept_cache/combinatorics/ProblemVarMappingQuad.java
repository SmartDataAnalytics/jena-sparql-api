package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.isomorphism.Problem;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformSignaturize;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;


public class ProblemVarMappingQuad
    extends ProblemMappingEquivBase<Quad, Quad, Var, Var>
{
    protected Set<Set<Expr>> aCnf;
    protected Set<Set<Expr>> bCnf;

    // The canonical cnf common to both sets of quads (essentially this is the key by which the quads were grouped)
    // This is obtained by remapping all variables of aCnf and bCnf with the same specific constant
    //protected Set<Set<Expr>> canonicalCnf;

    /**
     * The constraints that apply to the given quads
     */
    protected Set<Set<Expr>> cnf;

    public ProblemVarMappingQuad(Collection<? extends Quad> as, Collection<? extends Quad> bs, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
        //throw new RuntimeException("Should not be used anymore");
    }

//    public ProblemVarMappingQuad(Collection<? extends Quad> as, Set<Set<Expr>> aCnf, Collection<? extends Quad> bs, Set<Set<Expr>> bCnf, Map<Var, Var> baseSolution) {
//        super(as, bs, baseSolution);
//        this.aCnf = aCnf;
//        this.bCnf = bCnf;
//    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions() {
        Iterable<Map<Var, Var>> tmp = CombinatoricsUtils.createSolutions(as, bs, baseSolution);
        Stream<Map<Var, Var>> result = StreamSupport.stream(tmp.spliterator(), false);
        return result;
    }

    /**
     * Signature construction:
     * - Variables that were already renamed must not be remapped again
     * - Variables of the 'as' that clash with the renaming (ps.values()) must be renamed first
     *
     *
     */
    @Override
    public Collection<Problem<Map<Var, Var>>> refine(Map<Var, Var> partialSolution) {

//        Set<Var> targetVars = SetUtils.asSet(partialSolution.values());
//        Set<Var> sourceVars = partialSolution.keySet();


        Collection<Problem<Map<Var, Var>>> result;

        System.out.println("Refining " + as + " - " + bs + " via " + partialSolution);

        boolean isCompatible = MapUtils.isPartiallyCompatible(baseSolution, partialSolution);
        if(!isCompatible) {
            result = Collections.emptySet(); //Collections.singleton(new ProblemUnsolvable<>());
        } else {

            Map<Var, Var> newBase = new HashMap<>();
            newBase.putAll(baseSolution);
            newBase.putAll(partialSolution);

            NodeTransform signaturizer = NodeTransformSignaturize.create(newBase);//partialSolution);

            Multimap<Quad, Quad> sigToAs = HashMultimap.create();
            as.forEach(q -> {
                Quad sig = NodeTransformLib.transform(signaturizer, q);
                sigToAs.put(sig, q);
            });

            Map<Var, Var> identity = partialSolution.values().stream().collect(Collectors.toMap(x -> x, x -> x));
            NodeTransform s2 = NodeTransformSignaturize.create(identity);
            Multimap<Quad, Quad> sigToBs = HashMultimap.create();
            bs.forEach(q -> {
                Quad sig = NodeTransformLib.transform(s2, q);
                sigToBs.put(sig, q);
            });

            Map<Quad, Entry<Set<Quad>, Set<Quad>>> group = ProblemVarMappingQuad.groupByKey(sigToAs.asMap(), sigToBs.asMap());

            System.out.println("sigToAs: " + sigToAs);
            System.out.println("sigToBs: " + sigToBs);
            group.values().stream().forEach(e ->
            System.out.println("  Refined to " + e + " from " + as + " - " + bs + " via " + partialSolution));

            result = group.values().stream()
                    .map(e -> new ProblemVarMappingQuad(e.getKey(), e.getValue(), newBase))
                    .collect(Collectors.toList());
        }

        return result;



//        Map<Var, Var> map = MapUtils.mergeIfCompatible(baseSolution, partialSolution);
//
//        Collection<Problem<Map<Var, Var>>> result;
//        if(map == null) {
//            result = Collections.emptySet();
//        } else {
            // TODO: Split the quads into equivalent classes

// From SparqlViewCache
//            public CacheResult lookup(QuadFilterPatternCanonical queryQfpc) { //PatternSummary queryPs) {
//                List<QfpcMatch> result = new ArrayList<QfpcMatch>();
//
//                // TODO: We need the quadToCnf map for the queryPs
//                IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(queryQfpc);

//            result = null;
//        }

//         = map == null
//                ? Collections.emptySet()
//                : Collections.singleton(new ProblemVarMappingQuad(as, bs, map))
//                ;
    }


    public static Multimap<Set<Expr>, Set<Expr>> signatureToClauses(Iterable<? extends Iterable<Expr>> clauses) {
        Multimap<Set<Expr>, Set<Expr>> result = HashMultimap.create();
        for(Iterable<Expr> clause : clauses) {
            Set<Expr> signature = ClauseUtils.signaturize(clause);
            Set<Expr> c = SetUtils.asSet(clause);
            result.put(signature, c);
        }

        return result;
    }


    /**
     *
     * { v1: (g, s, x, a),
     *   v2: (g, s, y, b) }
     *
     *  fn(s, x, g)
     *
     * { (a -> x), (b -> y) }
     *
     * label(join_v1_v2(v1, v2)):
     *
     *
     * every state can have a set of open problems and a flag of whether its parent may have open problems.
     *
     *
     *
     * @param quads
     * @param varMap
     */
    public static Collection<Collection<Quad>> refine(Collection<Quad> quads, Map<Var, Var> varMap) {
        return null;
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
        IBiSetMultimap<Set<Set<Expr>>, Quad> bCnfToQuad = bQuadToCnf.getInverse();

//        Set<Set<Set<Expr>>> keys = Sets.union(aCnfToQuad.keySet(), bCnfToQuad.keySet());
//        for()

        List<Problem<Map<Var, Var>>> problems = new ArrayList<>();
        Map<Set<Set<Expr>>, Entry<Set<Quad>, Set<Quad>>> quadGroups = groupByKey(aCnfToQuad, bCnfToQuad);

        for(Entry<Set<Quad>, Set<Quad>> quadGroup : quadGroups.values()) {
            Set<Quad> as = quadGroup.getKey();
            Set<Quad> bs = quadGroup.getValue();
            Problem<Map<Var, Var>> x = new ProblemVarMappingQuad(as, bs, varMap);
            problems.add(x);
        }


        Multimap<Set<Expr>, Set<Expr>> mapA = signatureToClauses(aQfpc.getFilterCnf());
        Multimap<Set<Expr>, Set<Expr>> mapB = signatureToClauses(bQfpc.getFilterCnf());

        Map<Set<Expr>, Entry<Set<Set<Expr>>, Set<Set<Expr>>>> exprGroups = groupByKey(mapA.asMap(), mapB.asMap());

        for(Entry<Set<Set<Expr>>, Set<Set<Expr>>> entry : exprGroups.values()) {
            Set<Set<Expr>> as = entry.getKey();
            Set<Set<Expr>> bs = entry.getValue();
            Problem<Map<Var, Var>> x = null; //new ProblemVarMappingExpr(as, bs, varMap);
            problems.add(x);
        }



        // re-index the expressions
        //SparqlCacheUtils.createM



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

    public static <K, V> Map<K, Entry<Set<V>, Set<V>>> groupByKey(Map<K, ? extends Iterable<V>> a, Map<K, ? extends Iterable<V>> b) {
        Map<K, Entry<Set<V>, Set<V>>> result = new HashMap<>();

        Set<K> keys = Sets.union(a.keySet(), b.keySet());
        keys.forEach(
                k -> {
                    Iterable<V> ax = a.get(k);
                    Iterable<V> bx = b.get(k);
                    Set<V> av = ax == null ? Collections.emptySet() : SetUtils.asSet(ax);
                    Set<V> bv = bx == null ? Collections.emptySet() : SetUtils.asSet(bx);

                    Entry<Set<V>, Set<V>> e = new SimpleEntry<>(av, bv);
                    result.put(k, e);
                }
            );

        return result;
    }

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
