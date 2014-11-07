package org.aksw.jena_sparql_api.concept_cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.domain.VarOccurrence;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.ReplaceConstants;
import org.apache.commons.math3.util.CombinatoricsUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.syntax.Element;

class ConceptMap
{

    //private Multimap<Set<Set<Expr>>, PatternSummary> quadCnfToSummary = HashMultimap.create();
    private IBiSetMultimap<Set<Set<Expr>>, PatternSummary> quadCnfToSummary = new BiHashMultimap<Set<Set<Expr>>, PatternSummary>();

    private Map<PatternSummary, Map<Set<Var>, Table>> cacheData = new HashMap<PatternSummary, Map<Set<Var>, Table>>();

    //private


    private void normalize(Expr expr) {
        if(expr.isFunction()) {
            ExprFunction fn = expr.getFunction();

            // TODO: If the function is symmetric
            // - Variables come before constants
            // - If both arguments are of same type, order them by value (constants) or name (variables)

        }
    }

    /**
     * ([?s ?s ?s ?o], (fn(?s, ?o))
     *
     * @param quad
     * @param expr
     */
    public static Set<Set<Expr>> normalize(Quad quad, Set<Set<Expr>> cnf) {
        List<Var> componentVars = Arrays.asList(Var.alloc("g"), Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));

        Map<Var, Var> renameMap = new HashMap<Var, Var>();
        Set<Set<Expr>> extra = new HashSet<Set<Expr>>();
        for(int i = 0; i < 4; ++i) {
            Var quadVar = (Var)QuadUtils.getNode(quad, i);
            Var componentVar = componentVars.get(i);

            Var priorComponentVar = renameMap.get(quadVar);
            // We need to rename
            if(priorComponentVar != null) {
                extra.add(Collections.<Expr>singleton(new E_Equals(new ExprVar(priorComponentVar), new ExprVar(componentVar))));
            } else {
                renameMap.put(quadVar, componentVar);
            }
        }

        NodeTransformRenameMap transform = new NodeTransformRenameMap(renameMap);
        Set<Set<Expr>> result = ClauseUtils.applyNodeTransformSet(cnf, transform);
        result.addAll(extra);

        //System.out.println(result);
        return result;
    }


    //private Map<Quad, >

    public void add(Concept concept, Object val) {
        Element element = concept.getElement();

        //com.hp.hpl.jena.sparql.algebra.optimize.

        // For each quad pattern, get the constraints for each component
        // alias: [ {cg}, {cs}, {cp}, {co} ]

        // ViewDefinitionNormalizerImpl
    }


    public void add(Collection<Quad> quads, Collection<Expr> exprs) {
    }

    public static Set<Set<Expr>> add(Quad quad, Set<Set<Expr>> cnf) {
        Set<Set<Expr>> result = new HashSet<Set<Expr>>();

        for(Set<Expr> clause : cnf) {
            Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);
            Set<Var> exprVars = QuadUtils.getVarsMentioned(quad);

            boolean isApplicable = exprVars.containsAll(clauseVars);
            if(isApplicable) {
                result.add(clause);
            }
        }

        return result;
    }


    public void lookup() {

    }


    public static QuadFilterPattern transform(Query query) {

        QuadFilterPattern result = null;

        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = ReplaceConstants.replace(op);

        if(op instanceof OpDistinct) {
            op = ((OpDistinct)op).getSubOp();
        }

        //OpProject opProject;
        if(op instanceof OpProject) {
            op = ((OpProject)op).getSubOp();
        }

        OpFilter opFilter;
        if(op instanceof OpFilter) {
            opFilter = (OpFilter)op;
        } else {
            opFilter = (OpFilter)OpFilter.filter(NodeValue.TRUE, op);
        }

        Op subOp = opFilter.getSubOp();

        if(subOp instanceof OpQuadPattern) {
            OpQuadPattern opQuadPattern = (OpQuadPattern)opFilter.getSubOp();

            QuadPattern quadPattern = opQuadPattern.getPattern();
            List<Quad> quads = quadPattern.getList();

            ExprList exprs = opFilter.getExprs();
            Expr expr = ExprUtils.andifyBalanced(exprs);

            result = new QuadFilterPattern(quads, expr);
        }


        return result;
    }

    public PatternSummary summarize(QuadFilterPattern originalPattern) {

        Expr expr = originalPattern.getExpr();
        Set<Quad> quads = new HashSet<Quad>(originalPattern.getQuads());


        Set<Set<Expr>> filterCnf = CnfUtils.toSetCnf(expr);

        // This is part of the result
        //List<Set<Set<Expr>>> quadCnfList = new ArrayList<Set<Set<Expr>>>(quads.size());
        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = new BiHashMultimap<Quad, Set<Set<Expr>>>();



        for(Quad quad : quads) {
            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Set<Expr>> cnf = new HashSet<Set<Expr>>(); //new HashSet<Clause>();

            for(Set<Expr> clause : filterCnf) {
                Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

                boolean containsAll = quadVars.containsAll(clauseVars);
                if(containsAll) {
                    cnf.add(clause);
                }
            }


            Set<Set<Expr>> quadCnf = normalize(quad, cnf);
            //quadCnfList.add(quadCnf);
            quadToCnf.put(quad, quadCnf);
        }

        // Iterate the quads again, and for each variable map it to where it to the component where it occurs in
        IBiSetMultimap<Var, VarOccurrence> varOccurrences = new BiHashMultimap<Var, VarOccurrence>();
        //for(int i = 0; i < quads.size(); ++i) {
            //Quad quad = quads.get(i);
        for(Quad quad : quads) {
            Set<Set<Expr>> quadCnf = quadToCnf.get(quad).iterator().next(); //quadCnfList.get(i);

            for(int j = 0; j < 4; ++j) {
                Var var = (Var)QuadUtils.getNode(quad, j);

                VarOccurrence varOccurence = new VarOccurrence(quadCnf, j);

                varOccurrences.put(var, varOccurence);
            }
        }

        //System.out.println("varOccurrences: " + varOccurrences);

        // Remove all variables that only occur in the same quad
        boolean pruneVarOccs = false;
        if(pruneVarOccs) {
            Iterator<Entry<Var, Collection<VarOccurrence>>> it = varOccurrences.asMap().entrySet().iterator();

            while(it.hasNext()) {
                Entry<Var, Collection<VarOccurrence>> entry = it.next();

                Set<Set<Set<Expr>>> varQuadCnfs = new HashSet<Set<Set<Expr>>>();
                for(VarOccurrence varOccurrence : entry.getValue()) {
                    varQuadCnfs.add(varOccurrence.getQuadCnf());

                    // Bail out early
                    if(varQuadCnfs.size() > 1) {
                        break;
                    }
                }

                if(varQuadCnfs.size() == 1) {
                    it.remove();
                }
            }
        }

        //Set<Set<Set<Expr>>> quadCnfs = new HashSet<Set<Set<Expr>>>(quadCnfList);

        QuadFilterPatternCanonical canonicalPattern = new QuadFilterPatternCanonical(quads, filterCnf);

        PatternSummary result = new PatternSummary(originalPattern, canonicalPattern, quadToCnf, varOccurrences);


        for(Entry<Var, Collection<VarOccurrence>> entry : varOccurrences.asMap().entrySet()) {
            //System.out.println("Summary: " + entry.getKey() + ": " + entry.getValue().size());
            //System.out.println(entry);
        }

        return result;
    }


    public void lookup(Query query) {
        System.out.println("LOOKUP: " + query);
        QuadFilterPattern qfp = transform(query);

        lookup(qfp);
    }

    public static <K, V> Set<V> mapSet(Set<K> set, Map<K, V> map) {
        Set<V> result = new HashSet<V>();
        for(K item : set) {
            V v = map.get(item);
            result.add(v);
        }

        return result;
    }

    public List<CacheHit> lookup(QuadFilterPattern qfp) {

        List<CacheHit> result = new ArrayList<CacheHit>();

        PatternSummary ps = summarize(qfp);

        Set<Set<Set<Expr>>> quadCnfs = ps.getQuadToCnf().getInverse().keySet();


        Set<PatternSummary> rawCands = new HashSet<PatternSummary>();

        int querySize = qfp.getQuads().size();

        for(Set<Set<Expr>> quadCnf : quadCnfs) {
            Collection<PatternSummary> cands = quadCnfToSummary.get(quadCnf);

            // TODO: Keep track of which variables are candidates -
            // e.g. variables that have only a single varOcc (i.e. appear only once in the query) cannot be used for caching

            // Only retain candidates having fewer quads than the query
            for(PatternSummary cand : cands) {
                int candSize = cand.getCanonicalPattern().getQuads().size();

                if(candSize <= querySize) {
                    rawCands.add(cand);
                }
            }

            //System.out.println("# candidates: " + candidates.size());

        }


        System.out.println("#cands: " + rawCands.size());



        for(PatternSummary cand : rawCands) {
            // Get the variable-combinations for which cache entries exist
            Map<Set<Var>, Table> tmp = cacheData.get(cand);
            Set<Set<Var>> candVarCombos = tmp.keySet();




            Iterator<Map<Var, Var>> varMaps = computeVarMapQuadBased(ps, cand, candVarCombos);

            //while(Map<Var, Var> varMap)
            while(varMaps.hasNext()) {
                Map<Var, Var> varMap = varMaps.next();

                NodeTransform rename = new NodeTransformRenameMap(varMap);

                QuadFilterPatternCanonical candRename = cand.getCanonicalPattern().applyNodeTransform(rename);

//                System.out.println(varMap);
//                System.out.println(candRename);
//                System.out.println(ps.getCanonicalPattern());

                boolean isSubsumed = candRename.isSubsumedBy(ps.getCanonicalPattern());
//                System.out.println("isSubsumed: " + isSubsumed);


                // If we found a subsumption, we need to finally check whether we can make use of it...
                // This means: no variable in the query that gets replaced by the cache pattern
                // must occur in any other triple or filter


                if(isSubsumed) {
                    Set<Var> candVars = candRename.getVarsMentioned();

                    //for(Set<Var> candVarCombo : candVarCombos) {
                    for(Entry<Set<Var>, Table> entry : tmp.entrySet()) {

                        Set<Var> candVarCombo = entry.getKey();
                        Set<Var> queryCandVarCombo = mapSet(candVarCombo, varMap);

                        //ResultSet rs = ResultSetFactory.copyResults(entry.getValue());
                        Table table = entry.getValue();
                        //ResultSet rs = table.toResultSet();

                        Set<Var> disallowedVars = Sets.difference(candVars, queryCandVarCombo);


                        QuadFilterPatternCanonical test = candRename.diff(ps.getCanonicalPattern());
                        Set<Var> testVars = test.getVarsMentioned();

                        Set<Var> cooccurs = Utils2.getCooccurrentVars(queryCandVarCombo, test.getQuads());

                        disallowedVars = Sets.difference(disallowedVars, cooccurs);

                        Set<Var> intersection = Sets.intersection(disallowedVars, testVars);


                        if(intersection.isEmpty()) {
                            CacheHit cacheHit = new CacheHit(test, table);
                            //Expr expr = createExpr(rs, varMap);

                            //test.getFilterCnf()
                            //test.getFilterCnf().add(Collections.singleton(expr));

                            // TODO Convert back into a quad filter pattern

                            result.add(cacheHit);
                        }

                    }


                }



            }
        }
        //IBiSetMultimap<K, V>
        return result;
    }

    public static Expr createExpr(ResultSet rs, Map<Var, Var> varMap) {

        //ResultSet copy = ResultSetFactory.copyResults(rs);

        Expr result;

        if(rs.getResultVars().size() == 1) {
            String varName = rs.getResultVars().iterator().next();
            Var var = Var.alloc(varName);

            Set<Node> nodes = getResultSetCol(rs, var);
            ExprList exprs = nodesToExprs(nodes);

            Var inVar = varMap.get(var);
            ExprVar ev = new ExprVar(inVar);

            result = new E_OneOf(ev, exprs);
        } else {
            throw new RuntimeException("Not supported yet");
        }

        return result;
    }

    public static Set<Node> getResultSetCol(ResultSet rs, Var v) {
        Set<Node> result = new HashSet<Node>();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node node = binding.get(v);

            if(node != null) {
                result.add(node);
            }
        }

        return result;
    }

    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr expr = NodeValue.makeNode(node);
            result.add(expr);
        }

        return result;
    }


    /**
     * Helper function to convert a multimap into a map.
     * Each key may only have at most one corresponding value,
     * otherwise an exception will be thrown.
     *
     * @param mm
     * @return
     */
    public static <K, V> Map<K, V> toMap(Map<K, ? extends Collection<V>> mm) {
        // Convert the multimap to an ordinate map
        Map<K, V> result = new HashMap<K, V>();
        for(Entry<K, ? extends Collection<V>> entry : mm.entrySet()) {
            K k = entry.getKey();
            Collection<V> vs = entry.getValue();

            if(!vs.isEmpty()) {
                if(vs.size() > 1) {
                    throw new RuntimeException("Ambigous mapping for " + k + ": " + vs);
                }

                V v = vs.iterator().next();
                result.put(k, v);
            }
        }

        return result;
    }

    public static int getNumMatches(QuadGroup quadGroup) {
        int result;

        int c = quadGroup.getCandQuads().size();
        int q = quadGroup.getQueryQuads().size();

        if(c > q) { // If there are more candidates quads than query quads, we can't map all of them
            result = 0;
        } else {
            result = (int)(CombinatoricsUtils.factorial(q) / CombinatoricsUtils.factorial(q - c));
        }

        return result;
    }


    /**
     * TODO this has complexity O(n^2)
     * We can surely do better than that because joins are sparse and we
     * don't have to consider quads that do not join...
     *
     *
     * @param sub
     * @return
     */
    public static SetMultimap<Quad, Quad> quadJoinSummary(List<Quad> sub) {

        Node[] tmp = new Node[4];

        SetMultimap<Quad, Quad> result = HashMultimap.create();
        for(int i = 0; i < sub.size(); ++i) {
            Quad a = sub.get(i);
            for(int j = i + 1; j < sub.size(); ++j) {
                Quad b = sub.get(j);

                for(int k = 0; k < 4; ++k) {
                    Node na = QuadUtils.getNode(a, k);
                    Node nb = QuadUtils.getNode(b, k);
                    boolean isEqual = na.equals(nb);
                    Node c = isEqual ? NodeValue.TRUE.asNode() : NodeValue.FALSE.asNode();

                    tmp[k] = c;
                }
                Quad summary = QuadUtils.create(tmp);

                result.put(summary, a);
                result.put(summary, b);
            }
        }

        return result;
    }

    /**
     * Find a mapping of variables from cand to query, such that the pattern of
     * cand becomes a subset of that of query
     *
     * null if no mapping can be established
     *
     * @param query
     * @param cand
     * @return
     */
    public Iterator<Map<Var, Var>> computeVarMapQuadBased(PatternSummary query, PatternSummary cand, Set<Set<Var>> candVarCombos) {

        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToCandQuad = cand.getQuadToCnf().getInverse();
        IBiSetMultimap<Set<Set<Expr>>, Quad> cnfToQueryQuad = query.getQuadToCnf().getInverse();

        //IBiSetMultimap<Quad, Quad> candToQuery = new BiHashMultimap<Quad, Quad>();
//        Map<Set<Set<Expr>>, QuadGroup> cnfToQuadGroup = new HashMap<Set<Set<Expr>>, QuadGroup>();
        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>();
        for(Entry<Set<Set<Expr>>, Collection<Quad>> entry : cnfToCandQuad.asMap().entrySet()) {

            //Quad candQuad = entry.getKey();
            Set<Set<Expr>> cnf = entry.getKey();

            Collection<Quad> candQuads = entry.getValue();
            Collection<Quad> queryQuads = cnfToQueryQuad.get(cnf);

            QuadGroup quadGroup = new QuadGroup(candQuads, queryQuads);
            quadGroups.add(quadGroup);

            // TODO We now have grouped together quad having the same constraint summary
            // Can we derive some additional constraints form the var occurrences?


//            SetMultimap<Quad, Quad> summaryToQuadsCand = quadJoinSummary(new ArrayList<Quad>(candQuads));
//            System.out.println("JoinSummaryCand: " + summaryToQuadsCand);
//
//            SetMultimap<Quad, Quad> summaryToQuadsQuery = quadJoinSummary(new ArrayList<Quad>(queryQuads));
//            System.out.println("JoinSummaryQuery: " + summaryToQuadsQuery);
//
//            for(Entry<Quad, Collection<Quad>> candEntry : summaryToQuadsCand.asMap().entrySet()) {
//                queryQuads = summaryToQuadsQuery.get(candEntry.getKey());
//
//                // TODO What if the mapping is empty?
//                QuadGroup group = new QuadGroup(candEntry.getValue(), queryQuads);
//
//                cnfToQuadGroup.put(cnf, group);
//            }
        }

        // Figure out which quads have ambiguous mappings

//        for(Entry<Set<Set<Expr>>, QuadGroup>entry : cnfToQuadGroup.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }

        // Order the quad groups by number of candidates - least number of candidates first
//        List<QuadGroup> quadGroups = new ArrayList<QuadGroup>(cnfToQuadGroup.values());
        Collections.sort(quadGroups, new Comparator<QuadGroup>() {
            @Override
            public int compare(QuadGroup a, QuadGroup b) {
                int i = getNumMatches(a);
                int j = getNumMatches(b);
                int r = j - i;
                return r;
            }
        });


        List<Iterable<Map<Var, Var>>> cartesian = new ArrayList<Iterable<Map<Var, Var>>>(quadGroups.size());

        // TODO Somehow obtain a base mapping
        Map<Var, Var> baseMapping = Collections.<Var, Var>emptyMap();

        for(QuadGroup quadGroup : quadGroups) {
            Iterable<Map<Var, Var>> it = IterableVarMapQuadGroup.create(quadGroup, baseMapping);
            cartesian.add(it);
        }

        CartesianProduct<Map<Var, Var>> cart = new CartesianProduct<Map<Var,Var>>(cartesian);

        Iterator<Map<Var, Var>> result = new IteratorVarMapQuadGroups(cart.iterator());

        return result;
    }

    public static <K, V> Map<K, V> mergeCompatible(Iterable<Map<K, V>> maps) {
        Map<K, V> result = new HashMap<K, V>();

        for(Map<K, V> map : maps) {
            if(MapUtils.isPartiallyCompatible(map, result)) {
                result.putAll(map);
            } else {
                result = null;
                break;
            }
        }

        return result;
    }

    public Map<Var, Var> computeVarMap(PatternSummary query, PatternSummary cand, Set<Set<Var>> candVarCombos) {


        // Determine the set of variables on which the cache could operate
        // TODO This should be part of the pattern summary - or a parent of it
//        IBiSetMultimap<Var, VarOccurrence> varOccs = query.getVarOccurrences();
//        Set<Var> applicableVars = new HashSet<Var>();
//
//        System.out.println("varOccs: " + varOccs);
//
//        for(Entry<Var, Collection<VarOccurrence>> entry : varOccs.asMap().entrySet()) {
//            System.out.println("ENTRY: " + entry);
//            if(entry.getValue().size() == 1) {
//                continue;
//            }
//
//            applicableVars.add(entry.getKey());
//        }
//


        //System.out.println("CandVarCombos: " + candVarCombos);



        // var occurrences of a cand's var must be a subset of that of the query

        IBiSetMultimap<VarOccurrence, Var> candVos = cand.getVarOccurrences().getInverse();
        IBiSetMultimap<VarOccurrence, Var> queryVos = query.getVarOccurrences().getInverse();


        //SetMultimap<Var, Var> candToQuery = HashMultimap.create();

        boolean abort = false;


        //final Map<Var, Set<Var>> candToQuery = new HashMap<Var, Set<Var>>();
        final IBiSetMultimap<Var, Var> candToQuery = new BiHashMultimap<Var, Var>();
        for(Entry<VarOccurrence, Collection<Var>> candEntry : candVos.asMap().entrySet()) {
            Set<Var> candVars = (Set<Var>)candEntry.getValue();
            VarOccurrence candVo = candEntry.getKey();

            Set<Var> queryVars = queryVos.get(candVo);
//            if(queryVars.isEmpty()) {
//                abort = true;
//                break;
//            }

            for(Var candVar : candVars) {
                //Set<Var> union = candToQuery.get(candVar);
//                if(union == null) {
//                    union = queryVars;
//                }
//                else {
//                    union = new HashSet<Var>(Sets.union(candVars, union));
//                }

                // If the intersection is an empty set, we cannot create a mapping that satisfies the constraints
//                if(union.isEmpty()) {
//                    abort = true;
//                    break;
//                }

                candToQuery.putAll(candVar, queryVars);
            }

//            if(abort) {
//                break;
//            }
        }

//        if(true) {
//            System.out.println("CandToQuery: " + candToQuery);
//            System.out.println("queryToCand: " + candToQuery.getInverse());
//            return null;
//        }

        // TODO Deal with ambiguous mappings

        // Order candidates by number of candidates
        List<Var> varOrder = new ArrayList<Var>(candToQuery.keySet());
        Collections.sort(varOrder, new Comparator<Var>() {
            @Override
            public int compare(Var a, Var b) {
                int i = candToQuery.get(a).size();
                int j = candToQuery.get(b).size();
                int r = j - i;
                return r;
            }
        });

        //backtrackMeh(query, cand, candToQuery, varOrder, 0);



        Map<Var, Var> result;

        if(abort) {
            result = null;
        } else {
            result = toMap(candToQuery.asMap());
        }



        return result;
    }

    public static void backtrackMeh(PatternSummary query, PatternSummary cand, Map<Var, Set<Var>> candToQuery, List<Var> varOrder, int index) {
        Var var = varOrder.get(index);

        Set<Var> queryVars = candToQuery.get(var);

        // Try a mapping, and backtrack if we hit a dead end
        for(Var queryVar : queryVars) {

            //

        }

    }


    public static Table createTable(ResultSet rs) {

        List<Var> vars = VarUtils.toList(rs.getResultVars());

        Table result = TableFactory.create(vars);

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            result.addBinding(binding);
        }

        return result;
    }



    public void index(Query query, ResultSet rs) {

        Table table = createTable(rs);

        QuadFilterPattern qfp = transform(query);
        PatternSummary ps = summarize(qfp);

        Set<Var> vars = VarUtils.toSet(rs.getResultVars());

        Map<Set<Var>, Table> map = cacheData.get(qfp);
        if(map == null) {
            map = new HashMap<Set<Var>, Table>();
            cacheData.put(ps, map);
        }

        Table tmp = map.get(vars);
        if(tmp != null) {
            throw new RuntimeException("Already cached data for result set");
        }

        map.put(vars, table);


        // Index the pattern summary

        //CacheSummary<ResultSet> cacheEntry = new CacheSummary<ResultSet>(ps, );


        Set<Set<Set<Expr>>> quadCnfs = ps.getQuadToCnf().getInverse().keySet();
        for(Set<Set<Expr>> quadCnf : quadCnfs) {
            quadCnfToSummary.put(quadCnf, ps);
        }




        //ps.get

        //ElementTreeAnalyser eta = new ElementTreeAnalyser(
        //ElementTreeAnalyser.


        // Index the constraints
//        RestrictionManagerImpl rm = new RestrictionManagerImpl();
//        Set<Clause> clauses = new HashSet<Clause>();
//        for(Set<Expr> set : cnf) {
//            clauses.add(new Clause(set));
//        }
//
//        NestedNormalForm nnf = new NestedNormalForm(clauses);
//        rm.stateCnf(nnf);


        // For each quad determine the clauses that only affect the quad

        // {?s ?s ?s ?s . Filter(?s In (...)} }
        // ?s => { 0, 1, 2, 3} // could use bit set

//        System.out.println("----------------------------");
//        for(Entry<Set<Set<Expr>>, Collection<PatternSummary>> entry : quadCnfToSummary.asMap().entrySet()) {
//            System.out.println(entry.getKey());
//
//            for(PatternSummary q : entry.getValue()) {
//                System.out.println("- " + q.getCanonicalPattern());
//            }
//
//            System.out.println("----------------------------");
//        }

        //System.out.println(conditionsToQuery);
    }


    // Return the variables that we cannot optimize away, as they
    // are referenced in the following portions
    // - projection
    // - order
    // - group by
    public static Set<Var> getRefVars(Query query) {
        //query.getProjectVars();
        return null;
    }
}