package org.aksw.jena_sparql_api.sparql.ext.xml;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Use the property function ?s xml:unnest (xpathexpr ?o) for dealing with (/iterating) NodeList
 * Use the function xml:path(?doc, pathexpr) to extract individual values
 * 
 * @author raven Mar 2, 2018
 *
 */
public class E_XPath
	extends FunctionBase2
{	
	private static final Logger logger = LoggerFactory.getLogger(E_XPath.class);

	
	protected XPath xPath;

	public E_XPath() {
		 this(XPathFactory.newInstance().newXPath());
	}
	
	public E_XPath(XPath xPath) {
		 this.xPath = xPath;
	}
	

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {
        NodeValue result;

        // TODO If a node is of type string, we could still try to parse it as xml for convenience
        
        Object obj = nv.asNode().getLiteralValue();
    	if(obj instanceof Node) {
	    	Node xml = (Node)obj;
	    	
	        if(query.isString() && xml != null) {
	        	String queryStr = query.getString();	        	
	        		        	
	            try {
	            	XPathExpression expr = xPath.compile(queryStr);
	            	Object tmp = expr.evaluate(xml, XPathConstants.STRING);
	            	
	            	if(tmp instanceof NodeList) {
	            		NodeList nodes = (NodeList)tmp;
	            		for(int i = 0; i < nodes.getLength(); ++i) {
	            			Node node = nodes.item(i);
	            			System.out.println("" + node);
	            		}
	            	}
	            	
		        	//Object tmp = xPath.evaluate(queryStr, xml, XPathConstants.STRING);
		        	// FIXME Hack
		        	result = NodeValue.makeString("" + tmp);
	            } catch(Exception e) {
	                logger.warn(e.getLocalizedMessage());
	                result = NodeValue.nvNothing;
	            }
	        } else {
	        	result = NodeValue.nvNothing; //Expr.NONE.getConstant();
	        }
        } else {
            result = NodeValue.nvNothing; //Expr.NONE.getConstant();
        }

        return result;
    }

}
