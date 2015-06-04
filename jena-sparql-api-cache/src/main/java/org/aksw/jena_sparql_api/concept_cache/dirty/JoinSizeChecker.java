package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceBuilder;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.utils.CnfUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.lang.ParserSPARQL11;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;


/*
class QuadFilter {
    private Quad quad;
    private QuadFilterPatternCanonical qfp;
}
*/

class CountInfoComparator
    implements Comparator<CountInfo>
{
    @Override
    public int compare(CountInfo a, CountInfo b) {
        int result = doCompare(a, b);
        return result;
    }

    // Sort known item counts first
    public static int doCompare(CountInfo a, CountInfo b) {
        int ah = a.isHasMoreItems() ? 1 : -1;
        int bh = b.isHasMoreItems() ? 1 : -1;
        int x = bh - ah;
        long y = b.getCount() - a.getCount();

        int r = x == 0 ? (int)y : x;

        return r;
    }
}


public class JoinSizeChecker {
    public static CountInfo fetchCount(QueryExecutionFactory qef, Element e, Long limit) {
        Var vc = Var.alloc("_c_");

        Long l = limit != null ? limit + 1 : null;

        Query query = createQueryCount(vc, e, l);
        Node node = QueryExecutionUtils.executeSingle(qef, query, vc);
        Number value = (Number)node.getLiteralValue();
        long c = value.longValue();

        boolean hasMoreItems = limit != null ? c >= l : false;
        long v = hasMoreItems ? c - 1 : c;

        CountInfo result = new CountInfo(v, hasMoreItems, limit);

        return result;
    }

    public static Element limitElement(Element e, long limit) {
        Query subQuery = new Query();
        subQuery.setQuerySelectType();
        subQuery.setQueryResultStar(true);
        subQuery.setQueryPattern(e);
        subQuery.setLimit(limit);

        Element result = new ElementSubQuery(subQuery);
        return result;
    }

    public static Query createQueryCount(Var countVar, Element e, Long limit) {
        if(limit != null) {
            e = limitElement(e, limit);
        }

        Var tmpVar = Var.alloc(countVar.getName() + "_tmp_");

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(countVar, new ExprAggregator(tmpVar, new AggCount()));
        result.setQueryPattern(e);

        return result;
    }

    public static Element toElement(Quad quad, Set<Set<Expr>> cnf) {
        ExprList exprs = CnfUtils.toExprList(cnf);

        BasicPattern bp = new BasicPattern();
        bp.add(quad.asTriple());
        OpQuadPattern opA = new OpQuadPattern(quad.getGraph(), bp);
        Op opB = OpFilter.filter(exprs, opA);

        Query query = OpAsQuery.asQuery(opB);
        Element result = query.getQueryPattern();

        return result;
    }

    /**
     *
     * @param qef
     * @param qfp
     */
    public static Map<Quad, CountInfo> analyze(QueryExecutionFactory qef, QuadFilterPattern qfp, Long limit) {
        Map<Quad, Set<Set<Expr>>> quadToCnf = ConceptMap.quadToCnf(qfp);

        Map<Quad, CountInfo> result = analyze(qef, quadToCnf, limit);

        return result;
    }

    public static List<Quad> orderBySelectivity(Map<Quad, CountInfo> quadToCountInfo) {

        return null;
    }


    public static Map<Quad, CountInfo> analyze(QueryExecutionFactory qef, Map<Quad, Set<Set<Expr>>> quadToCnf, Long limit) {
        Map<Quad, CountInfo> result = new HashMap<Quad, CountInfo>();

        for(Entry<Quad, Set<Set<Expr>>> entry : quadToCnf.entrySet()) {
            Quad quad = entry.getKey();

            Element e = toElement(quad, entry.getValue());
            System.out.println(e);

            CountInfo countInfo = fetchCount(qef, e, limit);
            result.put(quad, countInfo);
        }

        return result;
    }



    /*
     * Given a quad filter pattern (i.e. a set of quads and corresponding filters in CNF),
     * create a QuadFilter for each quad.
     */
    public static void partition(QuadFilterPattern qfp) {


    }



//    public static void analyze(QueryExecutionFactory qef, Element a, Element b, Long memberLimit, Long joinLimit) {
//        long ca = fetchCount(qef, a, memberLimit);
//        long cb = fetchCount(qef, b, memberLimit);
//
//        ElementGroup group = new ElementGroup();
//        group.addElement(a);
//        group.addElement(b);
//
//        long cc = fetchCount(qef, group, joinLimit);
//
//
//        // Both sides of the join must be large in size
//        // and the result of the join must be small
//
//    }

    public static void main(String[] args) {
        QueryExecutionFactory qef =
                SparqlServiceBuilder
                .http("http://dbpedia.org/sparql", "http://dbpedia.org")
                .create();

        Element a = ParserSPARQL11.parseElement("{ ?s a <http://dbpedia.org/ontology/Museum> }");
        Element b = ParserSPARQL11.parseElement("{ ?s <http://dbpedia.org/ontology/location> <http://dbpedia.org/resource/Leipzig> }");

        ElementGroup c = new ElementGroup();
        c.addElement(a);
        c.addElement(b);


        QuadFilterPattern qfp = ConceptMap.transform(c);
        Map<Quad, CountInfo> map = analyze(qef, qfp, 1000l);
        System.out.println(map);

        /*
        CountInfo cia = fetchCount(qef, a, null);
        System.out.println(cia);

        CountInfo cib = fetchCount(qef, b, null);
        System.out.println(cib);

        CountInfo cic = fetchCount(qef, c, null);
        System.out.println(cic);
        */
    }
}
