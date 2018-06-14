package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ModelUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

import com.google.common.collect.Iterables;

import io.reactivex.Flowable;


/**
 * LookupService for fetching models related to resources
 * @author raven
 *
 */
public class LookupServiceSparqlConstruct
    implements LookupService<Node, Model>
{
    private QueryExecutionFactory qef;
    private Query query;
    private Var var;

    public LookupServiceSparqlConstruct(QueryExecutionFactory qef, Query query, Var var) {
        this.qef = qef;
        this.query = query;
        this.var = var;
    }

    @Override
    public Flowable<Entry<Node, Model>> apply(Iterable<Node> keys) {
        //System.out.println("Lookup Request with " + Iterables.size(keys) + " keys: " + keys);

        Map<Node, Model> result = new HashMap<Node, Model>();

        if(!Iterables.isEmpty(keys)) {

            ExprList exprs = new ExprList();
            for(Node key : keys) {
                Expr e = NodeValue.makeNode(key);
                exprs.add(e);
            }


            E_OneOf expr = new E_OneOf(new ExprVar(var), exprs);
            Element filterElement = new ElementFilter(expr);

            Query q = query.cloneQuery();
            Element newElement = ElementUtils.mergeElements(q.getQueryPattern(), filterElement);
            q.setQueryPattern(newElement);

            //System.out.println("Lookup query: " + q);

            QueryExecution qe = qef.createQueryExecution(q);
            Model fullModel = qe.execConstruct();

            Iterator<Node> it = keys.iterator();
            while(it.hasNext()) {
                Node key = it.next();

                Resource s = new ResourceImpl(key, (ModelCom)fullModel);
                Model tmp = ModelUtils.filterBySubject(fullModel, s);
                result.put(key, tmp);
            }
        }

        return result;
    }
}
