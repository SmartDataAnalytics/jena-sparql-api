package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.mapper.Acc;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.AggLiteral;
import org.aksw.jena_sparql_api.mapper.AggMap;
import org.aksw.jena_sparql_api.mapper.AggTransform;
import org.aksw.jena_sparql_api.mapper.AggUtils;
import org.aksw.jena_sparql_api.mapper.BindingMapperProjectVar;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinSummaryService2Impl
    implements JoinSummaryService2
{
    private static final Logger logger = LoggerFactory.getLogger(JoinSummaryService2Impl.class);
    protected QueryExecutionFactory qef;

    public static final Query fwdQuery = QueryFactory.parse(new Query(), "PREFIX o: <http://example.org/ontology/> SELECT ?y (SUM(<http://www.w3.org/2001/XMLSchema#double>(?fy) / <http://www.w3.org/2001/XMLSchema#double>(?fx)) As ?z) { ?s o:sourcePredicate ?x ; o:targetPredicate ?y ; o:freqSource ?fx ; o:freqTarget ?fy } GROUP BY ?y", "http://example.org/base/", Syntax.syntaxARQ);
    public static final Query bwdQuery = QueryFactory.parse(new Query(), "PREFIX o: <http://example.org/ontology/> SELECT ?x (SUM(<http://www.w3.org/2001/XMLSchema#double>(?fx) / <http://www.w3.org/2001/XMLSchema#double>(?fy)) As ?z) { ?s o:sourcePredicate ?x ; o:targetPredicate ?y ; o:freqSource ?fx ; o:freqTarget ?fy } GROUP BY ?x", "http://example.org/base/", Syntax.syntaxARQ);

    public JoinSummaryService2Impl(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    @Override
    public Map<Node, Number> fetchPredicates(Iterable<Node> predicates, boolean reverse) {

        Var source = !reverse ? Vars.x : Vars.y;
        Var target = !reverse ? Vars.y : Vars.x;
        Var freq = Vars.z;


        Query query = !reverse ? fwdQuery : bwdQuery;
        ElementFilter element = new ElementFilter(new E_OneOf(new ExprVar(source), ExprListUtils.nodesToExprs(predicates)));
        QueryUtils.injectElement(query, element);
        System.out.println(query);

        Agg<Map<Node, Number>> agg = AggMap.create(new BindingMapperProjectVar(target), AggTransform.create(AggLiteral.create(BindingMapperProjectVar.create(freq)), (node) -> {
            Number r;
            // TODO Make a bug report that sometimes double rdf terms in json serialization in virtuoso 7.2.2 turn up as NAN
            try {
                r = (Number)node.getLiteralValue();
            } catch(Exception e) {
                logger.warn("Not a numeric literal: " + node);
                r = 1.0;
            }
            return r;
        }));

        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        Map<Node, Number> result = AggUtils.accumulate(agg, rs);

        return result;
    }

}
