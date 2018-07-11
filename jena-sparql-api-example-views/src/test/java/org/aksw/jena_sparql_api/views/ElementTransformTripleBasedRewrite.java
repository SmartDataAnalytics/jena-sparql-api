package org.aksw.jena_sparql_api.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.transform.ElementTransformVirtualPredicates;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
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

import com.google.common.collect.Iterables;

public abstract class ElementTransformTripleBasedRewrite
    extends ElementTransformCopyBase
{
    private static final Logger logger = LoggerFactory.getLogger(ElementTransformVirtualPredicates.class);

    // An element over the variables g, s, p, o

    public static Expr valueSetToExpr(ValueSet<Node> valueSet) {
    	ExprList el = ExprListUtils.nodesToExprs(valueSet.getValue());
    	
    	Expr core = new E_OneOf(new ExprVar(Vars.p), el);
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
            if(tp.isTriple()) {
                Triple triple = tp.asTriple();

//                Generator<Var> varGen = rootVarGen.clone();
                Element e = applyTripleTransform(triple); //, filter, varGen);
                if(e == null) {
                    newPattern.addTriple(new TriplePath(triple));
                } else {
                    elements.add(e);
                }
            } else {
                logger.warn("Triple path expressions not supported");
                newPattern.addTriple(tp);
            }
        }

        Iterable<Element> items = newPattern.isEmpty()
                ? elements
                : Iterables.concat(Collections.singleton(newPattern), elements)
                ;

        Element result = ElementUtils.createElementGroup(items);
        return result;
    }
    
    
    public abstract Element applyTripleTransform(Triple t);


}
