package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.Vars;

import com.google.common.base.Functions;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

public class LookupServiceListService<V>
    implements LookupService<Node, V>
{
    private MapService<Concept, Node, V> listService;

    public LookupServiceListService(MapService<Concept, Node, V> listService) {
        super();
        this.listService = listService;
    }

    @Override
    public Map<Node, V> apply(Iterable<Node> nodes) {
        ExprList args = new ExprList();
        for(Node node : nodes) {
            Expr expr = NodeValue.makeNode(node);
            args.add(expr);
        }

        Expr e = new E_OneOf(new ExprVar(Vars.s), args);
        Element filter = new ElementFilter(e);
        Concept concept = new Concept(filter, Vars.s);

        Map<Node, V> result = listService.fetchData(concept, null, null);
        return result;
    }


    public static <V> LookupServiceListService<V> create(MapService<Concept, Node, V> listService) {
        LookupServiceListService<V> result = new LookupServiceListService<>(listService);
        return result;
    }
}
