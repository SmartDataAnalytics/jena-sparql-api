package org.aksw.jena_sparql_api.utils.transform;

import java.util.Collection;
import java.util.Stack;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransform;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformCopyBase;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransform;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

/**
 * Rewrites a query's dataset description so that it becomes part of the query pattern.
 *
 * @author raven
 *
 */
public class ElementTransformDatasetDescription
    extends ElementTransformCopyBase
{
    protected ExprList defaultGraphExprs;
    protected ExprList namedGraphExprs;
    protected Stack<Node> graphs;

    protected Generator<Var> varGen;

    public ElementTransformDatasetDescription(Stack<Node> graphs, Generator<Var> varGen, ExprList defaultGraphExprs, ExprList namedGraphExprs) {
        this.graphs = graphs;
        this.varGen = varGen;
        this.defaultGraphExprs = defaultGraphExprs;
        this.namedGraphExprs = namedGraphExprs;
    }

    public static ElementTransformDatasetDescription create(Stack<Node> graphs, Element e, DatasetDescription dd) {
        Collection<Var> vars = PatternVars.vars(e);
        Generator<Var> varGen = VarGeneratorBlacklist.create("v", vars);

        ExprList defaultGraphExprs = ExprListUtils.fromUris(dd.getDefaultGraphURIs());
        ExprList namedGraphExprs = ExprListUtils.fromUris(dd.getNamedGraphURIs());

        ElementTransformDatasetDescription result = new ElementTransformDatasetDescription(graphs, varGen, defaultGraphExprs, namedGraphExprs);
        return result;
    }

    @Override
    public Element transform(ElementTriplesBlock el) {
        Element result = applyDefaultGraphs(el);
        return result;
    }

    @Override
    public Element transform(ElementPathBlock el) {
        Element result = applyDefaultGraphs(el);
        return result;
    }

    public Element applyDefaultGraphs(Element el) {
        Element result;

        // If there are no graphs, inject a graph block constrained to
        // the default graphs
        if(graphs.isEmpty() && !defaultGraphExprs.isEmpty()) {
            Var v = varGen.next();
            result = applyGraphs(varGen, v, el, defaultGraphExprs);
        } else {
            result = el;
        }

        return result;
    }

    public static Element applyGraphs(Generator<Var> varGen, Node gn, Element elt1, ExprList exprs) {
        //System.out.println("apply " + gn);
        Element result;

        if(!exprs.isEmpty()) {
            Var v;
            ExprList tmp;
            if(gn.isURI() || gn.isLiteral()) {
                v = varGen.next();
                tmp = new ExprList();
                tmp.add(NodeValue.makeNode(gn));
                tmp.addAll(exprs);
            } else if(gn.isVariable()) {
                v = (Var)gn;
                tmp = exprs;
            } else if(gn.isBlank()) {
                v = varGen.next();
                tmp = exprs;
            } else {
                throw new RuntimeException("Unexpected case");
            }

            ExprVar ev = new ExprVar(v);

            Element el = new ElementNamedGraph(v, elt1);
            ElementFilter filter = new ElementFilter(new E_OneOf(ev, exprs));

            ElementGroup group = new ElementGroup();
            group.addElement(el);
            group.addElement(filter);

            result = group;
        } else {
            result = new ElementNamedGraph(gn, elt1);
        }

        return result;
    }

    @Override
    public Element transform(ElementNamedGraph el, Node gn, Element elt1) {
        Element result = applyGraphs(varGen, gn, elt1, namedGraphExprs);
        return result;
    }

    public static Query rewrite(Query query) {
        DatasetDescription dd = query.getDatasetDescription();
        Query result;
        if(dd != null) {
            result = query.cloneQuery();
            Element before = result.getQueryPattern();
            Element after = rewrite(before, dd);
            result.setQueryPattern(after);
        } else {
            result = query;
        }

        return result;
    }

    public static Element rewrite(Element element, DatasetDescription dd) {
        final Stack<Node> graphs = new Stack<Node>();

        ExprTransform exprTransform = new ExprTransformCopy();
        ElementTransform elementTransform = ElementTransformDatasetDescription.create(graphs, element, dd);
        ElementVisitor beforeVisitor = new ElementVisitorBase() {
            @Override
            public void visit(ElementNamedGraph el) {
                graphs.push(el.getGraphNameNode());
                //System.out.println("push " + el.getGraphNameNode());
            }
        };
        ElementVisitor afterVisitor = new ElementVisitorBase() {
            @Override
            public void visit(ElementNamedGraph el) {
                graphs.pop();
                //System.out.println("pop " + el.getGraphNameNode());
            }
        };

        Element result = ElementTransformer.transform(element, elementTransform, exprTransform, beforeVisitor, afterVisitor);

        return result;
    }

    public static void main(String[] args) {
        //Query query = QueryFactory.create("SELECT * { { ?s ?p ?o } Union { Graph ?g { ?s ?p ?o } } }");
        //Query query = QueryFactory.create("SELECT * { { { Select * { ?s ?p ?o . Filter(?p = <p>) } } } Union { Graph ?g { ?s ?p ?o } } }");
        Query query = QueryFactory.create("SELECT * { { ?s ?p ?o . Graph ?x { ?a ?b ?c } } Union { Graph ?g { ?s ?p ?o } } }");
        query.addGraphURI("dg1");
        query.addGraphURI("dg2");
        query.addNamedGraphURI("ng1");
        query.addNamedGraphURI("ng2");

        Query tmp = rewrite(query);

        Op op = Algebra.compile(tmp);
        Op op2 = Transformer.transformSkipService(new TransformFilterPlacement(), op) ;
        tmp = OpAsQuery.asQuery(op2);

        System.out.println(tmp);
    }
}
