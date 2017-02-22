package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.persistence.criteria.Path;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

public class ExpressionCompiler
	implements ExpressionVisitor<Expr>
{
	protected TypeMapper typeMapper;
	protected Function<? super Path<?>, Relation> pathHandler;
	
	// 
	
	// The elements assembled from the expressions
	protected List<Element> elements = new ArrayList<>(); 
	
	
	public List<Element> getElements() {
		return elements;
	}
	
	public ExpressionCompiler(Function<? super Path<?>, Relation> pathHandler) {
		super();
		this.pathHandler = pathHandler;
		this.typeMapper = TypeMapper.getInstance();
	}

	public Expr visit(Path<?> e) {
		Relation relation = pathHandler.apply(e);
		
		Element element = relation.getElement();
		if(element != null) {
			elements.add(element);
		}
		
		Expr result = new ExprVar(relation.getTargetVar());
		return result;
	};
	
	@Override
	public Expr visit(EqualsExpression e) {
		Expr a = e.getLeftHandOperand().accept(this);
		Expr b = e.getRightHandOperand().accept(this);

		Expr result = new E_Equals(a, b);
		
		elements.add(new ElementFilter(result));
		
		return result;
	}

	@Override
	public Expr visit(LogicalNotExpression e) {
		Expr a = e.accept(this);
		Expr result = new E_LogicalNot(a);
		return result;
	}

	@Override
	public Expr visit(GreatestExpression<?> e) {
		// Prepare a sub-query
		
		//Expr expr = e.getOperand().accept(visitor);
		return null;
	}
	
	@Override
	public Expr visit(ValueExpression<?> e) {
		Object value = e.getValue();
		RDFDatatype rdfDatatype = typeMapper.getTypeByValue(value);
		Node node = NodeFactory.createLiteralByValue(value, rdfDatatype);
		NodeValue result = NodeValue.makeNode(node);
		return result;		
	}
	
	
	@Override
	public Expr visit(LogicalAndExpression e) {
		Expr a = e.getLeftHandOperand().accept(this);
		Expr b = e.getRightHandOperand().accept(this);
		
		// TODO This way of implementation is certainly wrong - do it right 
		return new E_LogicalAnd(a, b);
	}
}
