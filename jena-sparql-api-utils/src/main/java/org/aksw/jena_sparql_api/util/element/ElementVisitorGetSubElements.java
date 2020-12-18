package org.aksw.jena_sparql_api.util.element;

import java.util.Collections;
import java.util.List;

import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementFind;
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
import org.apache.jena.sparql.syntax.ElementVisitor;



/**
 * An element visitor that returns an element's immediate list of sub elements.
 * If there are none then the result is an empty list.
 * 
 * @author raven
 *
 */
public class ElementVisitorGetSubElements
	implements ElementVisitor
{
	public static List<Element> getSubElements(Element element) {
		ElementVisitorGetSubElements visitor = new ElementVisitorGetSubElements();
		element.visit(visitor);
		List<Element> result = visitor.getResult();
		return result;
	}
	
	
	protected List<Element> result;
	
	
	public List<Element> getResult() {
		return result;
	}
	@Override public void visit(ElementTriplesBlock el) { result = Collections.emptyList(); }
	@Override public void visit(ElementPathBlock el) { result = Collections.emptyList(); }
	@Override public void visit(ElementFilter el) { result = Collections.emptyList(); }
	@Override public void visit(ElementAssign el) { result = Collections.emptyList(); }
	@Override public void visit(ElementBind el) { result = Collections.emptyList(); }
	@Override public void visit(ElementFind el) { result = Collections.emptyList(); }
	@Override public void visit(ElementData el) { result = Collections.emptyList(); }
	@Override public void visit(ElementUnion el) { result = el.getElements(); }
	@Override public void visit(ElementOptional el) { result = Collections.singletonList(el.getOptionalElement()); }
	@Override public void visit(ElementGroup el) { result = el.getElements(); }
	@Override public void visit(ElementDataset el) { result = Collections.singletonList(el.getElement()); }
	@Override public void visit(ElementNamedGraph el) { result = Collections.singletonList(el.getElement()); }
	@Override public void visit(ElementExists el) { result = Collections.singletonList(el.getElement()); }
	@Override public void visit(ElementNotExists el) { result = Collections.singletonList(el.getElement()); }
	@Override public void visit(ElementMinus el) { result = Collections.singletonList(el.getMinusElement()); }
	@Override public void visit(ElementService el) { result = Collections.singletonList(el.getElement()); }
	
	@Override
	public void visit(ElementSubQuery el) {
		Element subElement = el.getQuery().getQueryPattern();
		result = subElement == null ? Collections.emptyList() : Collections.singletonList(subElement);
	}
}
