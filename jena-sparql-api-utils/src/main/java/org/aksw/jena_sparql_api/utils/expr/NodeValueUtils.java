package org.aksw.jena_sparql_api.utils.expr;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeValueUtils {

	public static final NodeValue NV_ABSENT = NodeValue.makeNode(NodeUtils.nullUriNode);

    private static final Logger logger = LoggerFactory.getLogger(NodeValueUtils.class);
    
    public static int getInteger(NodeValue expr) {
        int result;
        if(expr.isInteger()) {
            result = expr.getInteger().intValue();
        }
        else if(expr.isDecimal()) {
            result = expr.getDecimal().intValue();
        }
        else {
            throw new RuntimeException("Not an integer value: " + expr);
        }

        return result;
    }

    public static Number getNumber(NodeValue expr) {
    	Object obj = getValue(expr);
    	
    	Number result = obj instanceof Number ? (Number)obj : null;
    	return result;
    }
    

    /** Attempt to return a Java object for the given NodeValue */
    public static Object getValue(NodeValue expr) {
        if(expr == null) {
            return Expr.NONE; // FIMXE Why don't we return return null???
        } else if(expr.isIRI()){
            //logger.debug("HACK - Uri constants should be converted to RdfTerms first");
            return expr.asNode().getURI();
        } else if(expr.isBoolean()) {
            return expr.getBoolean();
        } else if(expr.isNumber()) {
            if(expr.isDecimal()) {
                BigDecimal d = expr.getDecimal();
                if(d.scale() > 0) {
                    return d.doubleValue();
                } else {
                    return d.intValue();
                }
            }
            else if(expr.isDouble()) {
                return expr.getDouble();
            } else if(expr.isFloat()) {
                return expr.getFloat();
            } else {
                return expr.getDecimal().longValue();
            }
        } else if(expr.isString()) {
            return expr.getString();
        } else if(expr.isDateTime()) {
            return expr.getDateTime();
        }
//        } else if(expr instanceof NodeValueNode) {
//            //Node node = ((NodeValueNode)expr).ge
//        	NodeValue nvNothing = NodeValue.makeNode(NodeFactory.createBlankNode(NodeValue.strForUnNode));
//            if(expr.equals(nvNothing)) {
//                return null;
//            } else {
//                throw new RuntimeException("Unknow datatype of node: " + expr);
//            }
//        }
        else {
            throw new RuntimeException("Unknow datatype of constant: " + expr);
        }
    }

}