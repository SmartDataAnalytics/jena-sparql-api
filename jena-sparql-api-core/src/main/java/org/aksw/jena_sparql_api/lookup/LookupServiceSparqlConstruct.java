package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ModelUtils;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;


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
    public Map<Node, Model> apply(Iterable<Node> keys) {
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
