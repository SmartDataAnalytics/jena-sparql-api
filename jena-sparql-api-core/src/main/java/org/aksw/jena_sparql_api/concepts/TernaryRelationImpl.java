package org.aksw.jena_sparql_api.concepts;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

/**
 * A ternary relation - it can e.g. be joined with a triple pattern
 * 
 * @author raven Mar 7, 2018
 *
 */
public class TernaryRelationImpl
	implements TernaryRelation
{
	protected Var s;
	protected Var p;
	protected Var o;

	protected Element element;

	public TernaryRelationImpl(Var s, Var p, Var o, Element element) {
		super();
		this.s = s;
		this.p = p;
		this.o = o;
		this.element = element;
	}
	
	public List<Var> getVars() {
		return Arrays.asList(s, p, o);
	}
	
	public Var getS() {
		return s;
	}

	public Var getP() {
		return p;
	}

	public Var getO() {
		return o;
	}

	public Element getElement() {
		return element;
	}

//	public TernaryRelation filterP(Concept concept) {
//		
//	}

	/**
	 * TODO Make the API more generic to filter on arbitrary variables
	 * Something like relation.p().filter(...)
	 * 
	 * @param node
	 * @return
	 */
	public TernaryRelation filterP(Node node) {
		List<Element> es = ElementUtils.toElementList(element);
		es.add(new ElementFilter(new E_Equals(new ExprVar(p), NodeValue.makeNode(node))));
		TernaryRelation result = new TernaryRelationImpl(s, p, o, ElementUtils.groupIfNeeded(es));
		return result;
	}
	
	@Override
	public String toString() {
		return "TernaryRelation [s=" + s + ", p=" + p + ", o=" + o + ", element=" + element + "]";
	}
}
