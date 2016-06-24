package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.collections.MapUtils;
import org.aksw.isomorphism.IsoMapUtils;
import org.aksw.isomorphism.Problem;
import org.aksw.isomorphism.ProblemUnsolvable;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

// Multimap<Set<Set<>>>
// ContainmentMap

/**
 * Match two sets of clauses (a clause is treated as a conjunction) of expressions against each other
 *
 * In general: the query must be more specific than the cache, hence, the query may apply additional filters to the cache
 *
 * Query: DNF = { {?a = <foo>, ?b = <bar>, ?c = <boo>}, {?a = <foo>, ?b = <bar>, ?d = <boo>} }
 * Cache: DNF = { {?a = <foo>, ?b = <bar>}, {?a = <foo>, ?b = <baz>} }
 *
 * At least one cache clause must be fully contained in one of the query's DNF clauses.
 *
 *
 *
 * Query: CNF = { {?a = <foo>}, {?b = <bar>}, {?c = <boo>, ?d = <boo>} }
 * Cache: CNF = { {?a = <foo>}, {?b = <bar>, ?b = <baz>} }
 *
 *
 *
 *
 *
 * Each DNF clause of the query must contain a clause of the cache.
 *
 * For over CNF clause of the cache, for at least one literal there must exist a clause of the query that contains it
 *
 *
 *
 * All expressions of the first set must match to expressions of the second set
 *
 * @author raven
 *
 */
public class ProblemVarMappingExpr
    extends ProblemMappingEquivBase<Expr, Expr, Var, Var>
{


//    public ProblemVarMappingExpr(Collection<? extends Collection<Expr>> as,
//            Collection<? extends Collection<Expr>> bs, Map<Var, Var> baseSolution) {
//        super(as, bs, baseSolution);
//    }

    public ProblemVarMappingExpr(Collection<? extends Expr> as, Collection<? extends Expr> bs, Map<Var, Var> baseSolution) {
        super(as, bs, baseSolution);
    }

    @Override
    public Stream<Map<Var, Var>> generateSolutions() {

        Map<Var, Var> baseSolution = Collections.emptyMap();
//        Stream<Map<Var, Var>> result = IsoMapUtils.createSolutionStream(
//                as,
//                bs,
//                (x, y) -> createVarMap(x, y),
//                baseSolution);
        Stream<Map<Var, Var>> result = IsoMapUtils.createSolutionStream(
                as,
                bs,
                (x, y, bs) -> { return createVarMap(x, y, bs).map(z -> z.getVarMap()); },
                baseSolution);

        return result;
    }

    /**
     *
     * { (?a = ?b) } vs { (?x = ?y) }
     *
     * if partialSolution contained ?a -> ?z, then
     *
     *
     *
     */
    @Override
    public Collection<Problem<Map<Var, Var>>> refine(Map<Var, Var> partialSolution) {

        Collection<Problem<Map<Var, Var>>> result;

        boolean isCompatible = MapUtils.isPartiallyCompatible(baseSolution, partialSolution);
        if(!isCompatible) {
            result = Collections.emptySet(); //Collections.singleton(new ProblemUnsolvable<>());
        } else {

            Map<Var, Var> newBase = new HashMap<>();
            newBase.putAll(baseSolution);
            newBase.putAll(partialSolution);

            Multimap<Expr, Expr> sigToAs = HashMultimap.create();
            as.forEach(e -> {
                Expr sig = ExprUtils.signaturize(e, partialSolution);
                sigToAs.put(sig, e);
            });

            Map<Var, Var> identity = partialSolution.values().stream().collect(Collectors.toMap(x -> x, x -> x));
            Multimap<Expr, Expr> sigToBs = HashMultimap.create();
            bs.forEach(e -> {
                Expr sig = ExprUtils.signaturize(e, identity);
                sigToBs.put(sig, e);
            });

            Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = ProblemVarMappingQuad.groupByKey(sigToAs.asMap(), sigToBs.asMap());

            result = group.values().stream()
                    .map(e -> new ProblemVarMappingExpr(e.getKey(), e.getValue(), newBase))
                    .collect(Collectors.toList());
        }

        return result;
    }

//    public static Iterable<Map<Var, Var>> createSolutions(Collection<Expr> as, Collection<Expr> bs, Map<Var, Var> baseSolution) {
//        //Map<Var, Var> baseSolution = Collections.emptyMap();
//        Iterable<Map<Var, Var>> result =
//                () -> IsoMapUtils.createSolutionStream(
//                    as,
//                    bs,
//                    ProblemVarMappingExpr::createSingleVarMap,
//                    baseSolution);.iterator();
//
//        return result;
//    }

//    public static Map<Var, Var> createVarMap(Collection<Expr> as, Collection<Expr> bs) {
//        Map<Var, Var> baseVarMap = Collections.emptyMap();
//
//        // TODO Not finished
//        return baseVarMap;
//    }

//    public static Map<Var, Var> createSingleVarMap(Expr a, Expr b) {
//        Map<Var, Var> result = createVarMap(a, b).findFirst().map(x -> x.getVarMap()).orElse(null);
//        return result;
//    }
//
//    public static Stream<ExprMapSolution> createVarMap(Expr a, Expr b) {
//        Stream<ExprMapSolution> result = createVarMap(a, b, Collections.emptyMap());
//        return result;
//    }

    /**
     *
     * It is important to note that linearization causes the leaf nodes to be
     * matched first. Hence, inner nodes are only processed once variable mappings
     * have been obtained from the map.
     *
     * @param needle
     * @param b
     * @return
     */
    public static Stream<ExprMapSolution> createVarMap(Expr needleA, Expr haystackB, Map<Var, Var> baseSolution) {
        List<Expr> as = ExprUtils.linearizePrefix(needleA, Collections.singleton(null)).collect(Collectors.toList());
        List<Expr> bs = ExprUtils.linearizePrefix(haystackB, Collections.singleton(null)).collect(Collectors.toList());

        // Get the number of leafs of b
        //int n = ExprUtils.countLeafs(a);
        //int m = ExprUtils.countLeafs(b);
        int m = as.size();
        int n = bs.size();

        // If there is a match, we can continue by the size of m, as there cannot be another overlap
        //Collection<ExprMapSolution> result = new ArrayList<>();

//      IntStream.range(0, n - m).forEach(i -> {
//      });

        //for(int i = 0; i < n - m + 1; ++i) {
        Stream<ExprMapSolution> result = IntStream.range(0, n - m + 1)
            .mapToObj(i -> {
                Map<Var, Var> varMap = new HashMap<Var, Var>(baseSolution);
                Expr be = null;
                for(int j = 0; j < m; ++j) {
                //Stream<ExprMapSolution> r = IntStream.range(0, m).map(j -> {
                    Expr ae = as.get(j);
                    be = bs.get(i + j);
                    boolean isCompatible;
                    if(ae == null && be == null) {
                        isCompatible = true;
                    }
                    else if(ae == null || be == null) {
                        isCompatible = false;
                    }
                    else if(ae.isVariable() && be.isVariable()) {
                        Var av = ae.getExprVar().asVar();
                        Var bv = be.getExprVar().asVar();
                        // Add a mapping to varMap if it is compatible
                        Map<Var, Var> tmp = Collections.singletonMap(av, bv);
                        isCompatible = MapUtils.isCompatible(tmp, varMap);

                        varMap.putAll(tmp);
                    }
                    else if(ae.isConstant() && be.isConstant()) {
                        NodeValue ac = ae.getConstant();
                        NodeValue bc = be.getConstant();

                        isCompatible = ac.equals(bc);
                    }
                    else if(ae.isFunction() && be.isFunction()) {
                        // The function symbols must match - the sub-expressions were already matched
                        // TODO Deal with sparql EXIST
                        ExprFunction af = ae.getFunction();
                        ExprFunction bf = be.getFunction();

                        FunctionLabel al = af.getFunctionSymbol();
                        FunctionLabel bl = bf.getFunctionSymbol();

                        isCompatible = al.equals(bl);
                    }
                    else {
                        isCompatible = false;
                    }

                    if(!isCompatible) {
                        varMap = null;
                        break;
                    }
                }

                ExprMapSolution r = varMap == null
                        ? null
                        : new ExprMapSolution(varMap, needleA, haystackB, be);

                return r;
            })
            .filter(x -> !Objects.isNull(x))
            ;

        return result;
    }

    @Override
    public String toString() {
        return super.toString();
    }



}
