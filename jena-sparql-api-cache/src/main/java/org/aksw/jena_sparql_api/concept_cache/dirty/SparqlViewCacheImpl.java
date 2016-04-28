package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.CombinatoricsUtils;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.Utils2;
import org.aksw.jena_sparql_api.concept_cache.core.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.core.SetUtils;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.core.TableUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.domain.VarOccurrence;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * TODO Extract an interface from this class in order to support
 * different (possibly non-in-memory) backends
 *
 * @author raven
 *
 */
public class SparqlViewCacheImpl
    implements SparqlViewCache
{
    private static final Logger logger = LoggerFactory.getLogger(SparqlViewCacheImpl.class);

    //private Multimap<Set<Set<Expr>>, PatternSummary> quadCnfToSummary = HashMultimap.create();
    private IBiSetMultimap<Set<Set<Expr>>, QuadFilterPatternCanonical> quadCnfToSummary = new BiHashMultimap<Set<Set<Expr>>, QuadFilterPatternCanonical>();
    private Map<QuadFilterPatternCanonical, Map<Set<Var>, Table>> cacheData = new HashMap<QuadFilterPatternCanonical, Map<Set<Var>, Table>>();

    private Map<QuadFilterPatternCanonical, IBiSetMultimap<Quad, Set<Set<Expr>>>> qfpcToQuadToCnf = new HashMap<>();


//    public void lookup(Query query) {
//        //System.out.println("LOOKUP: " + query);
//        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(query);
//        QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
//
//        lookup(qfp);
//    }

//    public CacheResult lookup(QuadFilterPattern queryQfp) {
//        PatternSummary queryPs = SparqlCacheUtils.summarize(queryQfp);
//
//        CacheResult result = lookup(queryPs);
//        return result;
//    }

    public CacheResult lookup(QuadFilterPatternCanonical queryQfpc) { //PatternSummary queryPs) {
        List<QfpcMatch> result = new ArrayList<QfpcMatch>();

        // TODO: We need the quadToCnf map for the queryPs
        IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf = SparqlCacheUtils.createMapQuadsToFilters(queryQfpc);



        //QuadFilterPattern queryQfp = queryPs.getOriginalPattern();

        //Set<Set<Set<Expr>>> quadCnfs = queryPs.getQuadToCnf().getInverse().keySet();
        //int querySize = queryPs.getCanonicalPattern().getQuads().size();
        Set<Set<Set<Expr>>> quadCnfs = queryQuadToCnf.getInverse().keySet();
        int querySize = queryQfpc.getQuads().size();


        Set<QuadFilterPatternCanonical> rawCandsSet = new HashSet<>();

        //int querySize = queryQfp.getQuads().size();

        for(Set<Set<Expr>> quadCnf : quadCnfs) {
            Collection<QuadFilterPatternCanonical> cands = quadCnfToSummary.get(quadCnf);

            // TODO: Keep track of which variables are candidates -
            // e.g. variables that have only a single varOcc (i.e. appear only once in the query) cannot be used for caching

            // Only retain candidates having fewer quads than the query
            for(QuadFilterPatternCanonical cand : cands) {
                int candSize = cand.getQuads().size();

                if(candSize <= querySize) {
                    rawCandsSet.add(cand);
                }
            }

            //System.out.println("# candidates: " + candidates.size());

        }


        //System.out.println("#cands: " + rawCands.size());

        // Try candidates with largest potential overlap first
        List<QuadFilterPatternCanonical> rawCands = new ArrayList<QuadFilterPatternCanonical>(rawCandsSet);
        Collections.sort(rawCands, new Comparator<QuadFilterPatternCanonical>() {
            @Override
            public int compare(QuadFilterPatternCanonical a, QuadFilterPatternCanonical b) {
                int x = a.getQuads().size();
                int y = b.getQuads().size();

                int r = x - y;
                return r;
            }
        });


        // Iterate all candidate pattern summaries and
        // check for their containment in the query
        for(QuadFilterPatternCanonical cand : rawCands) {
            // Get the variable-combinations for which cache entries exist
            Map<Set<Var>, Table> tmp = cacheData.get(cand);
            Set<Set<Var>> candVarCombos = tmp.keySet();


            // For a pattern there might be multiple candidate variable mappings
            // Filter expressions are not considered at this stage
            IBiSetMultimap<Quad, Set<Set<Expr>>> cacheQuadToCnf = qfpcToQuadToCnf.get(cand);
            Stream<Map<Var, Var>> varMaps = CombinatoricsUtils.computeVarMapQuadBased(queryQuadToCnf, cacheQuadToCnf, candVarCombos);

            //while(varMaps.hasNext()) {
            varMaps.forEach(varMap -> {


                NodeTransform rename = new NodeTransformRenameMap(varMap);

                QuadFilterPatternCanonical candRename = cand.applyNodeTransform(rename);

//                System.out.println(varMap);
//                System.out.println(candRename);
//                System.out.println(ps.getCanonicalPattern());

                boolean isSubsumed = candRename.isSubsumedBy(queryQfpc);
//                System.out.println("isSubsumed: " + isSubsumed);


                // If we found a subsumption, we need to finally check whether we can make use of it...
                // This means: no variable in the query that gets replaced by the cache pattern
                // must occur in any other triple or filter
                //List<Table> tables = new ArrayList<Table>();


                if(isSubsumed) {
                    Set<Var> candVars = candRename.getVarsMentioned();

                    //for(Set<Var> candVarCombo : candVarCombos) {
                    for(Entry<Set<Var>, Table> entry : tmp.entrySet()) {

                        Set<Var> candVarCombo = entry.getKey();

                        Set<Var> queryCandVarCombo = SetUtils.mapSet(candVarCombo, varMap);

                        //ResultSet rs = ResultSetFactory.copyResults(entry.getValue());
                        Table table = entry.getValue();
                        //ResultSet rs = table.toResultSet();

                        Set<Var> disallowedVars = Sets.difference(candVars, queryCandVarCombo);


                        QuadFilterPatternCanonical diffPattern = candRename.diff(queryQfpc);
                        Set<Var> testVars = diffPattern.getVarsMentioned();

                        Set<Var> cooccurs = Utils2.getCooccurrentVars(queryCandVarCombo, diffPattern.getQuads());

                        disallowedVars = Sets.difference(disallowedVars, cooccurs);

                        Set<Var> intersection = Sets.intersection(disallowedVars, testVars);


                        if(intersection.isEmpty()) {



                            table = TableUtils.transform(table, rename);

                            QfpcMatch cacheHit = new QfpcMatch(candRename, diffPattern, table, varMap);
                            //Expr expr = createExpr(rs, varMap);

                            //test.getFilterCnf()
                            //test.getFilterCnf().add(Collections.singleton(expr));

                            // TODO Convert back into a quad filter pattern

                            result.add(cacheHit);
                        }

                    }
                }
                // We do not have to iterate additional var mappings if we found a subsumption
                //break;
            });
        }


        // TODO We need to return all subsumed candidates so that we can
        // properly establish subsumption relations



       logger.debug("CacheHits: " + result.size());


        // TODO This has square complexity - maybe we could do better
        List<QfpcMatch> argh = result;

        List<QfpcMatch> actualResult =
            argh.stream()
            .filter(a ->
                !argh.stream().anyMatch(b -> a != b && b.getDiffPattern().isSubsumedBy(a.getDiffPattern())))
            .collect(Collectors.toList());

        logger.debug("CacheHits after subsumtion: " + actualResult.size());


        //List<CacheHit> c = new ArrayList<CacheHit>();
        //CacheHit r = null;
        QuadFilterPatternCanonical replacementPattern = null;
        List<Table> tables = new ArrayList<Table>();
        for(QfpcMatch ch : actualResult) {

            logger.debug("VarMap: Cache to Query: " + ch.getVarMap());
            //ch.get
            if(replacementPattern == null) {
                 replacementPattern = ch.getDiffPattern();
                 tables.add(ch.getTable());
            } else {
                QuadFilterPatternCanonical diff = replacementPattern.diff(ch.getDiffPattern());
                if(!diff.equals(replacementPattern)) {
                    replacementPattern = diff;
                    tables.add(ch.getTable());
                }
            }
        }

        logger.debug("Tables: " + tables.size());

        //result =
        CacheResult cr;
        if(replacementPattern == null) {
            cr = null;
        } else {
            cr = new CacheResult(replacementPattern, tables);
        }

        return cr;
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
            result = SparqlCacheUtils.toMap(candToQuery.asMap());
        }



        return result;
    }


    public void index(Query query, ResultSet rs) {

        //Table table = createTable(rs);
        ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(query);
        QuadFilterPattern qfp = pqfp.getQuadFilterPattern();

        Table table = SparqlCacheUtils.createTable(rs);
        index(qfp, table);
    }


    public void index(QuadFilterPattern qfp, ResultSetPart rsp) {
        Table table = ResultSetPart.toTable(rsp);
        index(qfp, table);
    }


    public void index(QuadFilterPatternCanonical qfpc, Table table) {
        //PatternSummary ps = SparqlCacheUtils.summarize(qfp);


        Set<Var> vars = VarUtils.toSet(table.getVarNames()); //.getResultVars());

        Map<Set<Var>, Table> map = cacheData.get(qfpc);
        if(map == null) {
            map = new HashMap<Set<Var>, Table>();
            cacheData.put(qfpc, map);

            IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = SparqlCacheUtils.createMapQuadsToFilters(qfpc);
            qfpcToQuadToCnf.put(qfpc, quadToCnf);

            Set<Set<Set<Expr>>> quadCnfs = quadToCnf.getInverse().keySet();
            for(Set<Set<Expr>> quadCnf : quadCnfs) {
                quadCnfToSummary.put(quadCnf, qfpc);
            }
        }


        Table tmp = map.get(vars);
        if(tmp != null) {
            throw new RuntimeException("Already cached data for result set");
        }

        map.put(vars, table);
    }

    public void index(QuadFilterPattern qfp, Table table) {

        QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(qfp, VarGeneratorImpl2.create("v"));
        index(qfpc, table);
    }

}