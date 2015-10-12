package org.aksw.jena_sparql_api.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.ResultSetPart;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;


public class LookupServiceSparqlQuery
    implements LookupService<Node, ResultSetPart>
{
    private QueryExecutionFactory sparqlService;
    private Query query;
    private Var var;

    public LookupServiceSparqlQuery(QueryExecutionFactory sparqlService, Query query, Var var) {
        this.sparqlService = sparqlService;
        this.query = query;
        this.var = var;
    }

    @Override
    public Map<Node, ResultSetPart> apply(Iterable<Node> keys) {
        System.out.println("Lookup Request with " + Iterables.size(keys) + " keys: " + keys);

        Map<Node, ResultSetPart> result = new HashMap<Node, ResultSetPart>();

        if(!Iterables.isEmpty(keys)) {

            ExprList exprs = ExprListUtils.nodesToExprs(keys);

            E_OneOf expr = new E_OneOf(new ExprVar(var), exprs);
            Element filterElement = new ElementFilter(expr);

            Query q = query.cloneQuery();
            Element newElement = ElementUtils.mergeElements(q.getQueryPattern(), filterElement);
            q.setQueryPattern(newElement);

            System.out.println("Lookup query: " + q);

            QueryExecution qe = sparqlService.createQueryExecution(q);
            ResultSet rs = qe.execSelect();

            Map<Node, List<Binding>> map = new HashMap<Node, List<Binding>>();
            while(rs.hasNext()) {
                Binding binding = rs.nextBinding();

                Node key = binding.get(var);

                //ResultSetMem x = (ResultSetMem)result.get(key);
                List<Binding> x= map.get(key);
                if(x == null) {
                    //x = new ResultSetMem();
                    x = new ArrayList<Binding>();
                    map.put(key, x);
                }

                x.add(binding);
            }

            for(Entry<Node, List<Binding>> entry : map.entrySet()) {
                //ResultSetStream r = new ResultSetStream(rs.getResultVars(), null, entry.getValue().iterator());
                //ResultSetRewindable rsw = ResultSetFactory.makeRewindable(r);

                ResultSetPart rsp = new ResultSetPart(rs.getResultVars(), entry.getValue());

                result.put(entry.getKey(), rsp);
            }
        }

        return result;
    }
}
