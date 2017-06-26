package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

import com.google.common.collect.AbstractIterator;




/**
 * Iterator for the offsets of candidate sequences in disjunctions
 *
 * Returns the indexes of subLists of the haystack, which are compatible to the
 * items in the node list
 *
 * @author raven
 *
 */
class IteratorDiffDisjunction
    extends AbstractIterator<Integer>
{
    private OpDisjunction needle;
    private OpDisjunction haystack;
    private int currentIndex = 0;


    public IteratorDiffDisjunction(OpDisjunction needle, OpDisjunction haystack) {
        this.needle = needle;
        this.haystack = haystack;
    }


    @Override
    protected Integer computeNext() {
        int k = needle.size();
        int n = Math.max(0, haystack.size() - k);

        for(int i = currentIndex; i < n; ++i) {

            boolean isCandidate = true;
            for(int j = 0; j < k; ++k) {
                Op haystackChild = haystack.get(i);
                Op needleChild = needle.get(i + j);

                String needleType = needleChild.getClass().getName();
                String haystackType = haystackChild.getClass().getName();

                if(!needleType.equals(haystackType)) {
                    isCandidate = false;
                    break;
                }
            }

            if(isCandidate) {
                return i;
            }

        }

        return endOfData();
    }
}





public class DiffUtils {

    // OpVars.visibleVars(op)

    public static String getOpClass(Op op) {
        String result = op.getClass().getName();
        return result;
    }

    public static Iterator<Map<Var, Var>> diff(Op needle, Op haystack) {
        // TODO This method is slow, expand with explicit code
        Iterator<Map<Var, Var>> result;
        try {
            result = MultiMethod.invokeStatic(DiffUtils.class, "_diff", needle, haystack);
        } catch(Exception e) {
            result = null;
            // TODO If there is no method, then there simply is no handler for the types
            // but other exceptions must be re-thrown
        }



        return result;
        /*
        String nc = getOpClass(needle);
        String hc = getOpClass(haystack);

        Ops type = Ops.valueOf(Ops.class, nc);

        Object result;
        if(nc.equals(hc)) {

            switch(type) {
            case OpDistinct:
                result = diff((OpDistinct)needle, (OpDistinct)haystack);
                break;

            case OpDisjunction:
                result = diff((OpDisjunction)needle, (OpDisjunction)haystack);
                break;
            }




        }

*/


    }


    public Iterator<Map<Var, Var>> _diff(OpDistinct needle, OpDistinct haystack) {
        Iterator<Map<Var, Var>> result = diff(needle.getSubOp(), haystack.getSubOp());
        return result;
    }

//    public static void diff(QuadFilterPatternCanonical pattern, QuadFilterPatternCanonical haystack, Map<Var, Var> varMap) {
//        DiffUtils.findMatches(rawCachePattern, queryPattern, varMap, tables)
//
//    }





    /**
     *Check if any sequence of members in the needle occurr in the hackstack
     *
     * @param needle
     * @param haystack
     * @return
     */
//    public Iterator<Map<Var, Var>> _diff(OpDisjunction needle, OpDisjunction haystack) {
//        Iterator<Map<Var, Var>> result = new IteratorDiffDisjunction(needle, haystack);
//        return result;
////OpExtendAssign
//    }




//    public Iterator<Map<Var, Var>> getCandidateVarMaps(IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadToCnf, IBiSetMultimap<Quad, Set<Set<Expr>>> candQuadToCnf, Set<Set<Var>> candVarCombos) {
//        Set<Set<Set<Expr>>> quadCnfs = queryQuadToCnf.getInverse().keySet();  //queryPs.getQuadToCnf().getInverse().keySet();
//
//        // For a pattern there might be multiple candidate variable mappings
//        // Filter expressions are not considered at this stage
//        Iterator<Map<Var, Var>> result = CombinatoricsUtils.computeVarMapQuadBased(queryQuadToCnf, candQuadToCnf, candVarCombos);
//
//        return result;
//    }



//    public testSubsumption(QuadFilterPatternCanonical rawCachePattern, QuadFilterPatternCanonical queryPattern, Map<Var, Var> varMap) {
//        NodeTransform rename = new NodeTransformRenameMap(varMap);
//
//        QuadFilterPatternCanonical cachePattern = rawCachePattern.applyNodeTransform(rename);
//
//        boolean isSubsumed = cachePattern.isSubsumedBy(queryPattern);
//
//    }





    /**
     * Checks if the the cache and query pattern are equal under the
     * given variable mapping
     *
     * @param rawCachePattern
     * @param queryPattern
     * @param varMap
     */
    public static boolean isIsomorph(QuadFilterPatternCanonical rawCachePattern, QuadFilterPatternCanonical queryPattern, Map<Var, Var> varMap) {
        NodeTransform rename = new NodeTransformRenameMap(varMap);
        QuadFilterPatternCanonical cachePattern = rawCachePattern.applyNodeTransform(rename);

        boolean result = cachePattern.equals(queryPattern);
        return result;
    }




    /**
     * Given a cache pattern with a collection of corresponding tables,
     * a query pattern and a variable mapping between the cache and the query pattern,
     * find possible substitutions in the query with the cache
     *
     * @param rawCachePattern
     * @param queryPattern
     * @param varMap
     * @param tables
     * @return
     */
    /*
    public static List<QfpcMatch> findMatches(QuadFilterPatternCanonical rawCachePattern, QuadFilterPatternCanonical queryPattern, Map<Var, Var> varMap, Iterable<Table> tables) {
        List<QfpcMatch> result = new ArrayList<QfpcMatch>();


        NodeTransform rename = new NodeTransformRenameMap(varMap);
        QuadFilterPatternCanonical cachePattern = rawCachePattern.applyNodeTransform(rename);


        boolean isSubsumed = cachePattern.isSubsumedBy(queryPattern);
//      System.out.println("isSubsumed: " + isSubsumed);


      // If we found a subsumption, we need to finally check whether we can make use of it...
      // This means: no variable in the query that gets replaced by the cache pattern
      // must occur in any other triple or filter
      //List<Table> tables = new ArrayList<Table>();


        if(isSubsumed) {
            for(Table table : tables) {

                Set<Var> candVars = cachePattern.getVarsMentioned();


                Set<Var> patternVars = cachePattern.getVarsMentioned();

                Set<Var> rawTableVars = new HashSet<Var>(table.getVars());
                Set<Var> queryTableVars = SetUtils.mapSet(rawTableVars, varMap);

                Set<Var> disallowedVars = Sets.difference(patternVars, queryTableVars);


                QuadFilterPatternCanonical diffPattern = cachePattern.diff(queryPattern);
                Set<Var> testVars = diffPattern.getVarsMentioned();

                Set<Var> cooccurs = Utils2.getCooccurrentVars(queryTableVars, diffPattern.getQuads());

                disallowedVars = Sets.difference(disallowedVars, cooccurs);

                Set<Var> intersection = Sets.intersection(disallowedVars, testVars);


                if(intersection.isEmpty()) {


                    table = TableUtils.transform(table, rename);

                    QfpcMatch cacheHit = new QfpcMatch(cachePattern, diffPattern, table, varMap);
                    //Expr expr = createExpr(rs, varMap);

                    //test.getFilterCnf()
                    //test.getFilterCnf().add(Collections.singleton(expr));

                    // TODO Convert back into a quad filter pattern

                    result.add(cacheHit);

                }
            }
        }

        return result;
    }
    */

    public static Object find(Collection<Op> caches, Op request) {

        for(Op cache : caches) {
            diff(request, cache);
        }

        return null;
    }

    public static Op toOp(String queryStr) {
        Query query = QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11);
        Op result = Algebra.compile(query);
        return result;
    }

    public static int main(String[] args) {
        Op a = toOp("Select ?s { ?s a <Airport> }");
        Op b = toOp("Select ?x { ?s locatedIn <Leipzig>");
        Op c = toOp("Select ?x { ?s a <Airport> . ?s locatedIn <Leipzig>");

        Collection<Op> cache = new ArrayList<Op>();
        cache.add(a);
        cache.add(b);

        /*
         * For each each cache op, return its list of possible substitutions.
         *
         * issue: what if a substitution requires a transformation of the query?
         * how to represent a transformation?
         *    we probably need to track which sub-tree in the original query was transformed
         *    so we know, which parts were unchanged, and which one was modified
         *
         *
         *
         *
         *
         *
         *
         * Map<cacheOp, List<Op, Substitutions>>
         */
        Object candidates = find(cache, c);

        return 0;
    }
}
