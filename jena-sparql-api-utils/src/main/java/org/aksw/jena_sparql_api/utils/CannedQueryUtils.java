package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_IsBlank;
import com.hp.hpl.jena.sparql.expr.E_IsURI;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/1/11
 *         Time: 1:12 AM
 */
public class CannedQueryUtils {
    
    private static final Var g = Var.alloc("g");
	private static final Var s = Var.alloc("s");
	private static final Var p = Var.alloc("p");
	private static final Var o = Var.alloc("o");

	public static Query spoTemplate() {
        return spoTemplate(s, p, o);
    }

    public static Query spoTemplate(Node s, Node p, Node o)
    {
        Query query = QueryFactory.create();
        query.setQuerySelectType();

        Triple triple = new Triple(s, p, o);
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);
        query.setQueryPattern(group);

        if(s.isVariable()) {
            query.getProject().add(Var.alloc(s.getName()));
        }
        if(p.isVariable()) {
            query.getProject().add(Var.alloc(p.getName()));
        }
        if(o.isVariable()) {
            query.getProject().add(Var.alloc(o.getName()));
        }

        return query;
    }
    
    public static Query spoCountTemplate() {
        return spoCountTemplate(s, p, o);
    }

    public static Query spoCountTemplate(Node s, Node p, Node o)
    {
        Query query = QueryFactory.create();
        query.setQuerySelectType();

        Triple triple = new Triple(s, p, o);
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);
        query.setQueryPattern(group);

        if(s.isVariable()) {
            query.getProject().add(Var.alloc(s.getName()));
        }
        if(p.isVariable()) {
            query.getProject().add(Var.alloc(p.getName()));
        }
        if(o.isVariable()) {
            query.getProject().add(Var.alloc(o.getName()));
        }
        
        query.allocAggregate(new AggCount());
        
        return query;
    }



    public static Query constructBySubjects(Collection<Node> ss) {

        ExprVar vs = new ExprVar(s);

        Query query = QueryFactory.create();
        query.setQueryConstructType();
        query.setDistinct(true);
        Triple triple = new Triple(s, p, o);
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);

        List<Expr> exprs = new ArrayList<Expr>();
        for(Node item : ss) {
            if(!item.isURI()) {
                continue;
            }

            exprs.add(new E_Equals(vs, NodeValue.makeNode(item)));
        }

        if(exprs.isEmpty()) {
            return null;
        }

        Expr or = ExprUtils.orifyBalanced(exprs);
        group.addElementFilter(new ElementFilter(or));

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);
        query.setConstructTemplate(new Template(bgp));
        query.setQueryPattern(group);

        return query;
    }

    public static Query constructBySubject(Node s) {
        Triple triple = new Triple(s, p, o);

        BasicPattern basicPattern = new BasicPattern();
        basicPattern.add(triple);

        Template template = new Template(basicPattern);

        ElementGroup elementGroup = new ElementGroup();
        ElementPathBlock pathBlock = new ElementPathBlock();
        elementGroup.addElement(pathBlock);

        pathBlock.addTriple(triple);

        Query query = new Query();
        query.setQueryConstructType();
        query.setConstructTemplate(template);
        query.setQueryPattern(elementGroup);

        return query;
    }


    public static Query describe(Node node) {
        Query query = QueryFactory.create();
        query.setQueryDescribeType();
        query.getResultURIs().add(node);

        return query;
    }

    public static Query incoming(Node object) {
        return incoming("s", "p", object);
    }

    public static Query incoming(String varNameS, String varNameP, Node object) {
        Node s = Var.alloc(varNameS);
        Node p = Var.alloc(varNameP);

        return inOutTemplate(s, p, object);
    }

    public static Query outgoing(Node subject) {
        return outgoing(subject, "p", "o");
    }

    public static Query outgoing(Node subject, String varNameP, String varNameO)
    {
        Node p = Var.alloc(varNameP);
        Node o = Var.alloc(varNameO);

        return inOutTemplate(subject, p, o);
    }

    public static Query inOutTemplate(Node s, Node p, Node o)
    {
        Query query = QueryFactory.create();
        query.setQueryConstructType();
        query.setDistinct(true);
        Triple triple = new Triple(s, p, o);
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);

        // Avoid non-uris as objects
        if(o.isVariable()) {
            group.addElementFilter(new ElementFilter(new E_IsURI(new ExprVar(o))));
            group.addElementFilter(new ElementFilter(new E_LogicalNot(new E_IsBlank(new ExprVar(o)))));
        }

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);
        query.setConstructTemplate(new Template(bgp));
        query.setQueryPattern(group);

        return query;
    }
        
    
}
