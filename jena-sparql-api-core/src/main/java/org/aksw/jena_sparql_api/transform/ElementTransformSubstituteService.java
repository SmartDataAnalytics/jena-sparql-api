package org.aksw.jena_sparql_api.transform;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;


/**
 * Element transform that substitutes SERVICE with another element.
 * This allows for use of SERVICE elements as 'variables'.
 * For example, a convention could be to substitute services that start with a &lt;slot:someName&gt;.
 * 
 * @author raven
 *
 */
public class ElementTransformSubstituteService
	extends ElementTransformCopyBase
{
	protected Function<Node, Element> substitution;
	
	@Override
	public Element transform(ElementService el, Node service, Element elt1) {
		Node serviceNode = el.getServiceNode();
		
		substitution.apply(serviceNode);
		
		
		// TODO Auto-generated method stub
		return super.transform(el, service, elt1);
	}
}
