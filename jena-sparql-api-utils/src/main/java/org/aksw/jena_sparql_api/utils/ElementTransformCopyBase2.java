package org.aksw.jena_sparql_api.utils;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

/** Create a copy if the Element(s) below has changed.
 * This is a common base class for writing recursive {@link ElementTransform}
 * in conjunction with being applied by {@link ElementTransformer}.
 */
public class ElementTransformCopyBase2 implements ElementTransform {
    // Note the use of == as object pointer equality.

    protected boolean alwaysCopy = false ;

    public ElementTransformCopyBase2() {
    	this(false);
    }

    public ElementTransformCopyBase2(boolean alwaysCopy) {
		super();
		this.alwaysCopy = alwaysCopy;
	}

	@Override
    public Element transform(ElementTriplesBlock el) {
		
		
        return el ;
    }

    @Override
    public Element transform(ElementPathBlock el) {
//    	if ( alwaysCopy ) {
// TODO Add PathBlock ctor
//    		el = new ElementPathBlock(new PathBlock(el.getPattern()));    		
//    	}
    	
        return el ;
    }

    @Override
    public Element transform(ElementFilter el, Expr expr2) {
        if ( !alwaysCopy && el.getExpr() == expr2 )
            return el ;
        return new ElementFilter(expr2) ;
    }

    @Override
    public Element transform(ElementAssign el, Var v, Expr expr2) {
        if ( !alwaysCopy && el.getVar() == v && el.getExpr() == expr2 )
            return el ;
        return new ElementAssign(v, expr2) ;
    }

    @Override
    public Element transform(ElementBind el, Var v, Expr expr2) {
        if ( !alwaysCopy && el.getVar() == v && el.getExpr() == expr2 )
            return el ;
        return new ElementBind(v, expr2) ;
    }

    @Override
    public Element transform(ElementData el) {
        return el ;
    }

    @Override
    public Element transform(ElementUnion el, List<Element> elts) {
        if ( !alwaysCopy && el.getElements() == elts )
            return el ;
        ElementUnion el2 = new ElementUnion() ;
        el2.getElements().addAll(elts) ;
        return el2 ;
    }

    @Override
    public Element transform(ElementOptional el, Element elt1) {
        if ( !alwaysCopy && el.getOptionalElement() == elt1 )
            return el ;
        return new ElementOptional(elt1) ;
    }

    @Override
    public Element transform(ElementGroup el, List<Element> elts) {
        if ( !alwaysCopy && el.getElements() == elts )
            return el ;
        ElementGroup el2 = new ElementGroup() ;
        el2.getElements().addAll(elts) ;
        return el2 ;
    }

    @Override
    public Element transform(ElementDataset el, Element elt1) {
        if ( !alwaysCopy && el.getElement() == elt1 )
            return el ;
        return new ElementDataset(el.getDataset(), elt1) ;
    }

    @Override
    public Element transform(ElementNamedGraph el, Node gn, Element elt1) {
        if ( !alwaysCopy && el.getGraphNameNode() == gn && el.getElement() == elt1 )
            return el ;
        return new ElementNamedGraph(gn, elt1) ;
    }

    @Override
    public Element transform(ElementExists el, Element elt1) {
        if ( !alwaysCopy && el.getElement() == elt1 )
            return el ;
        return new ElementExists(elt1) ;
    }

    @Override
    public Element transform(ElementNotExists el, Element elt1) {
        if ( !alwaysCopy && el.getElement() == elt1 )
            return el ;
        return new ElementNotExists(elt1) ;
    }

    @Override
    public Element transform(ElementMinus el, Element elt1) {
        if ( !alwaysCopy && el.getMinusElement() == elt1 )
            return el ;
        return new ElementMinus(elt1) ;
    }

    @Override
    public Element transform(ElementService el, Node service, Element elt1) {
        if ( !alwaysCopy && el.getServiceNode() == service && el.getElement() == elt1 )
            return el ;
        return new ElementService(service, elt1, el.getSilent()) ;
    }

    @Override
    public Element transform(ElementSubQuery el, Query query) {
        if ( !alwaysCopy && el.getQuery() == query )
            return el ;
        return new ElementSubQuery(query) ;
    }
}
