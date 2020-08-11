package org.aksw.jena_sparql_api.concepts;

import java.util.List;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

public interface TernaryRelation
	extends Relation
{
	Var getS();
	Var getP();
	Var getO();
	
	

	/**
	 * TODO Make the API more generic to filter on arbitrary variables
	 * Something like relation.p().filter(...)
	 * 
	 * @param node
	 * @return
	 */
	default TernaryRelation filterP(Node node) {
		Element element = getElement();
		Var s = getS();
		Var p = getP();
		Var o = getO();

		List<Element> es = ElementUtils.toElementList(element);
		es.add(new ElementFilter(new E_Equals(new ExprVar(p), NodeValue.makeNode(node))));
		TernaryRelation result = new TernaryRelationImpl(ElementUtils.groupIfNeeded(es), s, p, o);
		return result;
	}
	

}
