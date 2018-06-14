package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import io.reactivex.Flowable;


public class LookupServiceSparqlQuery
    implements LookupService<Node, Table>
{
    private static final Logger logger = LoggerFactory.getLogger(LookupServiceSparqlQuery.class);

    protected QueryExecutionFactory sparqlService;
    protected Query query;
    protected Var var;

    public LookupServiceSparqlQuery(QueryExecutionFactory sparqlService, Query query, Var var) {
        this.sparqlService = sparqlService;
        this.query = query;
        this.var = var;
    }

 
    @Override
    public Flowable<Entry<Node, Table>> apply(Iterable<Node> keys) {
        //System.out.println("Lookup Request with " + Iterables.size(keys) + " keys: " + keys);

    	Flowable<Entry<Node, Table>> result;

        if(!Iterables.isEmpty(keys)) {

            ExprList exprs = ExprListUtils.nodesToExprs(keys);

            E_OneOf expr = new E_OneOf(new ExprVar(var), exprs);
            Element filterElement = new ElementFilter(expr);


            Query q = query.cloneQuery();
            Element newElement = ElementUtils.mergeElements(q.getQueryPattern(), filterElement);
            q.setQueryPattern(newElement);

            //System.out.println("Lookup query: " + q);
            logger.debug("Looking up: " + q);

            Map<Node, List<Binding>> map = new HashMap<Node, List<Binding>>();
           
                        
            result = ReactiveSparqlUtils.execSelect(() -> sparqlService.createQueryExecution(q))
            	.groupBy(b -> b.get(var))
            	.flatMapSingle(groups -> groups
            			.collectInto((Table)new TableN(), (t, b) -> t.addBinding(b))
            			.map(x -> (Entry<Node, Table>)Maps.immutableEntry(groups.getKey(), x)));
        } else {
        	result = Flowable.empty();
        }
            
//            CompletableFuture<ResultSet> future = new CompletableFuture<ResultSet>();
//
//            
//            QueryExecution qe = sparqlService.createQueryExecution(q);
//            //List<String> resultVars;
//            try {
//                ResultSet rs = qe.execSelect();
//                List<Var> resultVars = VarUtils.toList(rs.getResultVars()); //new ArrayList<String>(rs.getResultVars());
//
//                while(rs.hasNext()) {
//                    Binding binding = rs.nextBinding();
//
//                    Node key = binding.get(var);
//
//                    //ResultSetMem x = (ResultSetMem)result.get(key);
//                    List<Binding> x = map.get(key);
//                    if(x == null) {
//                        //x = new ResultSetMem();
//                        x = new ArrayList<Binding>();
//                        map.put(key, x);
//                    }
//
//                    x.add(binding);
//                }
//
//                for(Entry<Node, List<Binding>> entry : map.entrySet()) {
//                    //ResultSetStream r = new ResultSetStream(rs.getResultVars(), null, entry.getValue().iterator());
//                    //ResultSetRewindable rsw = ResultSetFactory.makeRewindable(r);
//                	Table table = new TableData(resultVars, entry.getValue());
//                    //ResultSetPart rsp = new ResultSetPart(resultVars, entry.getValue());
//
//                    result.put(entry.getKey(), table);
//                }
//            } finally {
//                qe.close();
//            }
//        }

        
        return result;
    }
}
