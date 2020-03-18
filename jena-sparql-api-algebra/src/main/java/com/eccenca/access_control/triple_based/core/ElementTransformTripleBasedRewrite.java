package com.eccenca.access_control.triple_based.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.commons.collections.ValueSet;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base transformation that forwards every encountered {@link Triple} and {@link TriplePath} object
 * to the concrete implementation.
 * 
 * 
 * 
 * At present does not support quads.
 * 
 * @author Claus Stadler, Sep 3, 2018
 *
 */
public abstract class ElementTransformTripleBasedRewrite
    extends ElementTransformCopyBase
{
    /**
     * 
     * @param t
     * @return A substituting element for the argument or null to retain the original {@link Triple}
     */
    public abstract Element applyTripleTransform(Triple t);

    /**
     * 
     * @param tp
     * @return A substituting element for the argument or null to retain the original {@link TriplePath}
     */
    public abstract Element applyTriplePathTransform(TriplePath tp);


	
    private static final Logger logger = LoggerFactory.getLogger(ElementTransformTripleBasedRewrite.class);

    // An element over the variables g, s, p, o

    public static Expr valueSetToExpr(ValueSet<Node> valueSet, Var var) {
    	ExprList el = ExprListUtils.nodesToExprs(valueSet.getValue());
    	
    	Expr core = new E_OneOf(new ExprVar(var), el);
    	Expr result = valueSet.isPositive() ? core : new E_LogicalNot(core);

    	return result;
    }


    @Override
    public Element transform(ElementTriplesBlock el) {
        BasicPattern bgp = el.getPattern();

        BasicPattern newPattern = new BasicPattern();
        List<Element> elements = new ArrayList<Element>(bgp.size());
        for(Triple triple : bgp) {
            //Generator<Var> varGenCopy = varGen.clone();
            Element e = applyTripleTransform(triple);
            if(e == null) {
                newPattern.add(triple);
            } else {
                elements.add(e);
            }
        }

        Iterable<Element> items = newPattern.isEmpty()
                ? elements
                : Iterables.concat(Collections.singleton(new ElementTriplesBlock(newPattern)), elements)
                ;


        Element result = ElementUtils.createElementGroup(items);
        return result;
    }
    
    @Override
    public Element transform(ElementPathBlock el) {
        PathBlock bgp = el.getPattern();

        ElementPathBlock newPattern = new ElementPathBlock();
        List<Element> elements = new ArrayList<Element>(bgp.size());
        for(TriplePath tp : bgp) {
        	Element e;
        	if(tp.isTriple()) {
                Triple triple = tp.asTriple();

//                Generator<Var> varGen = rootVarGen.clone();
                e = applyTripleTransform(triple); //, filter, varGen);
            	if(e == null) {
                    newPattern.addTriple(new TriplePath(triple));
                } else {
                    elements.add(e);
                }
            } else {
            	e = applyTriplePathTransform(tp);
            	if(e == null) {
                    newPattern.addTriple(tp);
                } else {
                    elements.add(e);
                }
            }
        }

        Iterable<Element> items = newPattern.isEmpty()
                ? elements
                : Iterables.concat(Collections.singleton(newPattern), elements)
                ;

        Element result = ElementUtils.createElementGroup(items);
        return result;
    }
}
