package org.aksw.jena_sparql_api.utils;

import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

/**
 * A copying transform that applies an ElementTransform syntax pattern of
 * E_Exist and E_NoExists
 * */
public class ExprTransformApplyElementTransform2 extends ExprTransformCopy
{
    private final ElementTransform transform ;
    
    public ExprTransformApplyElementTransform2(ElementTransform transform, boolean alwaysCopy) {
    	super(alwaysCopy);
        this.transform = transform ; 
    }
    
    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    {
        Element el2 = ElementTransformer.transform(funcOp.getElement(), transform) ;
        
        if ( el2 == funcOp.getElement() )
            return super.transform(funcOp, args, opArg) ;
        if ( funcOp instanceof E_Exists )
            return new E_Exists(el2) ;
        if ( funcOp instanceof E_NotExists )
            return new E_NotExists(el2) ;
        throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: \n"+funcOp) ;
    }
}