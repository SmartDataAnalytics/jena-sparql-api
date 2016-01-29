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
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;


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
        //System.out.println("Lookup Request with " + Iterables.size(keys) + " keys: " + keys);

        Map<Node, ResultSetPart> result = new HashMap<Node, ResultSetPart>();

        if(!Iterables.isEmpty(keys)) {

            ExprList exprs = ExprListUtils.nodesToExprs(keys);

            E_OneOf expr = new E_OneOf(new ExprVar(var), exprs);
            Element filterElement = new ElementFilter(expr);

            Query q = query.cloneQuery();
            Element newElement = ElementUtils.mergeElements(q.getQueryPattern(), filterElement);
            q.setQueryPattern(newElement);

            //System.out.println("Lookup query: " + q);

            Map<Node, List<Binding>> map = new HashMap<Node, List<Binding>>();
            QueryExecution qe = sparqlService.createQueryExecution(q);
            //List<String> resultVars;
            try {
                ResultSet rs = qe.execSelect();
                List<String> resultVars = new ArrayList<String>(rs.getResultVars());

                while(rs.hasNext()) {
                    Binding binding = rs.nextBinding();

                    Node key = binding.get(var);

                    //ResultSetMem x = (ResultSetMem)result.get(key);
                    List<Binding> x = map.get(key);
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

                    ResultSetPart rsp = new ResultSetPart(resultVars, entry.getValue());

                    result.put(entry.getKey(), rsp);
                }
            } finally {
                qe.close();
            }
        }

        return result;
    }
}
