package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.function.Function;

import javax.persistence.criteria.Path;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

public class ExpressionCompiler
	implements ExpressionVisitor<Expr>
{
	protected TypeMapper typeMapper;
	protected Function<? super Path<?>, Expr> pathHandler;
	
	public ExpressionCompiler(Function<? super Path<?>, Expr> pathHandler) {
		super();
		this.pathHandler = pathHandler;
		this.typeMapper = TypeMapper.getInstance();
	}

	public Expr visit(Path<?> e) {
		Expr result = pathHandler.apply(e);
		return result;
	};
	
	@Override
	public Expr visit(EqualsExpression e) {
		Expr a = e.getLeftHandOperand().accept(this);
		Expr b = e.getRightHandOperand().accept(this);

		Expr result = new E_Equals(a, b);
		return result;
	}

	@Override
	public Expr visit(LogicalNotPredicate e) {
		Expr a = e.accept(this);
		Expr result = new E_LogicalNot(a);
		return result;
	}

	@Override
	public Expr visit(ValueExpression<?> e) {
		Object value = e.getValue();
		RDFDatatype rdfDatatype = typeMapper.getTypeByValue(value);
		Node node = NodeFactory.createLiteralByValue(value, rdfDatatype);
		NodeValue result = NodeValue.makeNode(node);
		return result;		
	}
}
