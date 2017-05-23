package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.collections.multimaps.MultimapUtils;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.PatternSummary;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.algebra.utils.VarOccurrence;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.Utils2;
import org.aksw.jena_sparql_api.concept_cache.core.QfpcAggMatch;
import org.aksw.jena_sparql_api.concept_cache.core.TableUtils;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * TODO Extract an interface from this class in order to support
 * different (possibly non-in-memory) backends
 *
 * @author raven
 *
 */
public class SparqlViewMatcherQfpcImpl<K>
    implements SparqlViewMatcherQfpc<K>
{
    private static final Logger logger = LoggerFactory.getLogger(SparqlViewMatcherQfpcImpl.class);

    //private Multimap<Set<Set<Expr>>, PatternSummary> quadCnfToSummary = HashMultimap.create();
    private IBiSetMultimap<Set<Set<Expr>>, QuadFilterPatternCanonical> quadCnfToSummary = new BiHashMultimap<Set<Set<Expr>>, QuadFilterPatternCanonical>();
    //private Map<QuadFilterPatternCanonical, Map<Set<Var>, Table>> cacheData = new HashMap<QuadFilterPatternCanonical, Map<Set<Var>, Table>>();

    private Map<QuadFilterPatternCanonical, IBiSetMultimap<Quad, Set<Set<Expr>>>> qfpcToQuadToCnf = new HashMap<>();

    protected Map<K, QuadFilterPatternCanonical> keyToPattern = new HashMap<>();
    //protected Map<QuadFilterPatternCanonical, K> patternToKey = new IdentityHashMap<>();
    protected Multimap<QuadFilterPatternCanonical, K> qfpcToKeys = HashMultimap.create();


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

//    public CacheResult lookup(QuadFilterPatternCanonical queryQfpc) { //PatternSummary queryPs) {
//    	CacheResult result = lookup(queryQfpc, quadCnfToSummary, qfpcToQuadToCnf);
//    	return result;
//    }


    /**
     *
     *
     * @return
     */
    public Map<K, QfpcMatch> lookup(QuadFilterPatternCanonical queryQfpc) {
    	Map<K, QfpcMatch> result = lookupCore(queryQfpc, quadCnfToSummary, qfpcToQuadToCnf, qfpcToKeys);

//    	List<CacheResult2<V>> result = tmp.stream()
//    		.map(qfpcMatch -> new CacheResult2<>())
//    		.collect(Collectors.toList());
//
    	return result;
    }


//    public static <K> List<QfpcMatch<K>> filterSubsumption(Collection<QfpcMatch<K>> hits) {
//        // Prune subsumed results
//        List<QfpcMatch<K>> result =
//                hits.stream()
//                .filter(a ->
//                    !hits.stream().anyMatch(b -> a != b && b.getDiffPattern().isSubsumedBy(a.getDiffPattern())))
//                .collect(Collectors.toList());
//
//            logger.debug("CacheHits after subsumtion: " + result.size());
//
//
//
//        // TODO We need to return all subsumed candidates so that we can
//        // properly establish subsumption relations
//
//
//
//       logger.debug("CacheHits: " + result.size());
//
//       return result;
//    }

    public static <K> Map<K, QfpcMatch> filterSubsumption(Map<K, QfpcMatch> hits) {
        // Prune subsumed results
        Map<K, QfpcMatch> result =
                hits.entrySet().stream()
                .filter(a ->
                    !hits.entrySet().stream().anyMatch(b -> a != b && b.getValue().getDiffPattern().isSubsumedBy(a.getValue().getDiffPattern())))
                .collect(Collectors.toMap(
                		Entry::getKey,
                		Entry::getValue,
                		(x, y) -> { throw new AssertionError(); },
                		LinkedHashMap::new));

        logger.debug("CacheHits after subsumtion: " + result.size());



        // TODO We need to return all subsumed candidates so that we can
        // properly establish subsumption relations



       //logger.debug("CacheHits: " + result.size());

       return result;
    }




    public static <K> Map<K, QfpcMatch> lookupCore(
    		QuadFilterPatternCanonical queryQfpc,
    		IBiSetMultimap<Set<Set<Expr>>, QuadFilterPatternCanonical> quadDnfToSummary,
    		//Map<QuadFilterPatternCanonical, Map<Set<Var>, Table>> cacheData,
    		Map<QuadFilterPatternCanonical, IBiSetMultimap<Quad, Set<Set<Expr>>>> qfpcToQuadToCnf,
    		Multimap<QuadFilterPatternCanonical, K> qfpcToKeys
    ) {
        //List<QfpcMatch<K>> result = new ArrayList<>();
    	Map<K, QfpcMatch> result = new LinkedHashMap<>();

        // TODO: We need the quadToCnf map for the queryPs
        IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf = AlgebraUtils.createMapQuadsToFilters(queryQfpc);



        //QuadFilterPattern queryQfp = queryPs.getOriginalPattern();

        //Set<Set<Set<Expr>>> quadCnfs = queryPs.getQuadToCnf().getInverse().keySet();
        //int querySize = queryPs.getCanonicalPattern().getQuads().size();
        Set<Set<Set<Expr>>> quadCnfs = queryQuadToCnf.getInverse().keySet();
        int querySize = queryQfpc.getQuads().size();


        Set<QuadFilterPatternCanonical> rawCandsSet = new HashSet<>();

        //int querySize = queryQfp.getQuads().size();


        for(Set<Set<Expr>> quadCnf : quadCnfs) {
            Collection<QuadFilterPatternCanonical> cands = quadDnfToSummary.get(quadCnf);

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
        List<QuadFilterPatternCanonical> rawCands = new ArrayList<>(rawCandsSet);
        Collections.sort(rawCands, (a, b) -> {
        	int x = a.getQuads().size();
            int y = b.getQuads().size();

            int r = x - y;
            return r;
        });


        // Iterate all candidate pattern summaries and
        // check for their containment in the query
        for(QuadFilterPatternCanonical cand : rawCands) {
            // Get the variable-combinations for which cache entries exist
//            Map<Set<Var>, Table> tmp = cacheData.get(cand);
//            Set<Set<Var>> candVarCombos = tmp.keySet();


            // For a pattern there might be multiple candidate variable mappings
            // Filter expressions are not considered at this stage
            IBiSetMultimap<Quad, Set<Set<Expr>>> cacheQuadToCnf = qfpcToQuadToCnf.get(cand);

            cacheQuadToCnf.asMap().entrySet().forEach(e -> logger.debug("qfpcToQuadToCnf: " + e.getKey() + " -> " + e.getValue()));
            //Stream<Map<Var, Var>> varMaps = CombinatoricsUtils.computeVarMapQuadBased(cacheQuadToCnf, queryQuadToCnf, candVarCombos);
            Stream<Map<Var, Var>> varMaps = VarMapper.createVarMapCandidates(cand, queryQfpc);

            //while(varMaps.hasNext()) {
            varMaps.forEach(varMap -> {
                logger.debug("Processing candidate: " + varMap);


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
                    //QuadFilterPatternCanonical diffPattern = candRename.diff(queryQfpc);
                	QuadFilterPatternCanonical diffPattern = queryQfpc.diff(candRename);

                    Collection<K> keys = qfpcToKeys.get(cand);
                    for(K k : keys) {
                        //Set<Var> candVars = candRename.getVarsMentioned();
                        QfpcMatch cacheHit = new QfpcMatch(candRename, diffPattern, varMap);

                        //result.add(cacheHit);
                        result.put(k, cacheHit);

                    }

                    // validate the candidate
                }
                // We do not have to iterate additional var mappings if we found a subsumption
                //break;
            });
        }

//        List<QfpcMatch<K>> result = filterSubsumption(rawResult);

        return result;
    }

//    public static <K> QfpcMatch<K> lookup(
//    		QuadFilterPatternCanonical queryQfpc,
//    		IBiSetMultimap<Set<Set<Expr>>, QuadFilterPatternCanonical> quadCnfToSummary,
//    		//Map<QuadFilterPatternCanonical, Map<Set<Var>, Table>> cacheData,
//    		Map<QuadFilterPatternCanonical, IBiSetMultimap<Quad, Set<Set<Expr>>>> qfpcToQuadToCnf
//    ) {
//    	List<QfpcMatch<K>> actualResult = lookupCore(queryQfpc, quadCnfToSummary, qfpcToQuadToCnf, qfpcToKeys);
//
//        CacheResult foo = postProcessResult(actualResult);
//        return foo;
//    }


    public static <K> QfpcAggMatch<K> aggregateResults(Map<K, QfpcMatch> actualResult) {
        // TODO This has square complexity - maybe we could do better
        //List<QfpcMatch> argh = result;
//
//        List<QfpcMatch> actualResult =
//            argh.stream()
//            .filter(a ->
//                !argh.stream().anyMatch(b -> a != b && b.getDiffPattern().isSubsumedBy(a.getDiffPattern())))
//            .collect(Collectors.toList());
//
//        logger.debug("CacheHits after subsumtion: " + actualResult.size());


        //List<CacheHit> c = new ArrayList<CacheHit>();
        //CacheHit r = null;
        QuadFilterPatternCanonical replacementPattern = null;
        Set<K> keys = new LinkedHashSet<>();
        for(Entry<K, QfpcMatch> e : actualResult.entrySet()) {
        	K key = e.getKey();
        	QfpcMatch ch = e.getValue();

            logger.debug("VarMap: Cache to Query: " + ch.getVarMap());
            //ch.get
            if(replacementPattern == null) {
                 replacementPattern = ch.getDiffPattern();
                 keys.add(key);
            } else {
                QuadFilterPatternCanonical diff = replacementPattern.diff(ch.getDiffPattern());
                if(!diff.equals(replacementPattern)) {
                    replacementPattern = diff;
                    keys.add(key);
                }
            }
        }

        logger.debug("Keys: " + keys.size());

        //result =
        QfpcAggMatch<K> cr;
        if(replacementPattern == null) {
            cr = null;
        } else {
            cr = new QfpcAggMatch<>(replacementPattern, keys);
        }

        return cr;
    }


    // TODO Turn into a predicate
    public Set<Map<Var, Var>> validateCandidatesByProjectedVars(Map<Set<Var>, Table> tmp, QuadFilterPatternCanonical queryQfpc, QuadFilterPatternCanonical candRename, NodeTransform rename, Map<Var, Var> varMap) {
    	Set<Var> candVars = candRename.getVarsMentioned();

    	Set<Map<Var, Var>> result = new HashSet<>();

        //for(Set<Var> candVarCombo : candVarCombos) {
        for(Entry<Set<Var>, Table> entry : tmp.entrySet()) {

        	//Set<Map<Var, Var>> result

            //boolean isValidated = validateCandidateByProjectedVars(queryQfpc, candRename, varMap, candVars, entry.getKey());

        }

        //Set<Map<Var, Var>>

        return result;
    }


	public static boolean validateCandidateByProjectedVars(
			QuadFilterPatternCanonical viewQfpc,
			QuadFilterPatternCanonical queryQfpc,
			//NodeTransform rename,
			Map<Var, Var> varMap,
			//Set<Var> candVars,
			Set<Var> candVarCombo) {
			//Entry<Set<Var>, Table> entry) {
		//Set<Var> candVarCombo = entry.getKey();

        NodeTransform rename = new NodeTransformRenameMap(varMap);

        QuadFilterPatternCanonical renamedViewQfpc = viewQfpc.applyNodeTransform(rename);

		Set<Var> candVars = renamedViewQfpc.getVarsMentioned();

		Set<Var> queryCandVarCombo = SetUtils.mapSet(candVarCombo, varMap);

		//ResultSet rs = ResultSetFactory.copyResults(entry.getValue());
		//Table table = entry.getValue();
		//ResultSet rs = table.toResultSet();

		Set<Var> disallowedVars = Sets.difference(candVars, queryCandVarCombo);


		QuadFilterPatternCanonical diffPattern = renamedViewQfpc.diff(queryQfpc);
		Set<Var> testVars = diffPattern.getVarsMentioned();

		Set<Var> cooccurs = Utils2.getCooccurrentVars(queryCandVarCombo, diffPattern.getQuads());

		disallowedVars = Sets.difference(disallowedVars, cooccurs);

		Set<Var> intersection = Sets.intersection(disallowedVars, testVars);


		boolean result = intersection.isEmpty();
		return result;
//		if(intersection.isEmpty()) {
//
//
//
//		    //table = TableUtils.transform(table, rename);
//
//		    QfpcMatch cacheHit = new QfpcMatch(candRename, diffPattern, null, varMap);
//		    //Expr expr = createExpr(rs, varMap);
//
//		    //test.getFilterCnf()
//		    //test.getFilterCnf().add(Collections.singleton(expr));
//
//		    // TODO Convert back into a quad filter pattern
//
//		    //result.add(cacheHit);
//		    //result.add(varMap);
//
//		}
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
            result = MultimapUtils.toMap(candToQuery.asMap());
        }



        return result;
    }


    @Override
    public void put(K key, QuadFilterPatternCanonical qfpc) {

    	keyToPattern.put(key, qfpc);
    	qfpcToKeys.put(qfpc, key);

        //map = new HashMap<Set<Var>, Table>();
        //cacheData.put(qfpc, map);

        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = AlgebraUtils.createMapQuadsToFilters(qfpc);
        qfpcToQuadToCnf.put(qfpc, quadToCnf);

        Set<Set<Set<Expr>>> quadCnfs = quadToCnf.getInverse().keySet();
        for(Set<Set<Expr>> quadCnf : quadCnfs) {
            quadCnfToSummary.put(quadCnf, qfpc);
        }


//
////        Set<Var> vars = VarUtils.toSet(table.getVarNames()); //.getResultVars());
//
//        Map<Set<Var>, Table> map = cacheData.get(qfpc);
//        if(map == null) {
//            map = new HashMap<Set<Var>, Table>();
//            cacheData.put(qfpc, map);
//
//            IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = SparqlCacheUtils.createMapQuadsToFilters(qfpc);
//            qfpcToQuadToCnf.put(qfpc, quadToCnf);
//
//            Set<Set<Set<Expr>>> quadCnfs = quadToCnf.getInverse().keySet();
//            for(Set<Set<Expr>> quadCnf : quadCnfs) {
//                quadCnfToSummary.put(quadCnf, qfpc);
//            }
//        }
//
//
//        Table tmp = map.get(vars);
//        if(tmp != null) {
//            throw new RuntimeException("Already cached data for result set");
//        }
//
//        map.put(vars, table);
    }

    public void index(Query query, ResultSet rs) {

        //Table table = createTable(rs);
        ProjectedQuadFilterPattern pqfp = AlgebraUtils.transform(query);
        QuadFilterPattern qfp = pqfp.getQuadFilterPattern();

        Table table = TableUtils.createTable(rs);
        index(qfp, table);
    }


    public void index(QuadFilterPattern qfp, ResultSetPart rsp) {
        Table table = ResultSetPart.toTable(rsp);
        index(qfp, table);
    }


    public void index(QuadFilterPatternCanonical qfpc, Table table) {
        //PatternSummary ps = SparqlCacheUtils.summarize(qfp);

    	throw new RuntimeException("don't use");
//
//        Set<Var> vars = VarUtils.toSet(table.getVarNames()); //.getResultVars());
//
//        Map<Set<Var>, Table> map = cacheData.get(qfpc);
//        if(map == null) {
//            map = new HashMap<Set<Var>, Table>();
//            cacheData.put(qfpc, map);
//
//            IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = SparqlCacheUtils.createMapQuadsToFilters(qfpc);
//            qfpcToQuadToCnf.put(qfpc, quadToCnf);
//
//            Set<Set<Set<Expr>>> quadCnfs = quadToCnf.getInverse().keySet();
//            for(Set<Set<Expr>> quadCnf : quadCnfs) {
//                quadCnfToSummary.put(quadCnf, qfpc);
//            }
//        }
//
//
//        Table tmp = map.get(vars);
//        if(tmp != null) {
//            throw new RuntimeException("Already cached data for result set");
//        }
//
//        map.put(vars, table);
    }

    public void index(QuadFilterPattern qfp, Table table) {

        QuadFilterPatternCanonical qfpc = AlgebraUtils.canonicalize2(qfp, VarGeneratorImpl2.create("v"));
        index(qfpc, table);
    }


	@Override
	public void removeKey(Object key) {
		QuadFilterPatternCanonical qfpc = keyToPattern.get(key);
		if(qfpc != null) {
			qfpcToKeys.get(qfpc).remove(key);
			//qfpcToKeys.put(qfpc, key);


			qfpcToQuadToCnf.remove(qfpc);
			quadCnfToSummary.getInverse().removeAll(qfpc);
		}
	}

}