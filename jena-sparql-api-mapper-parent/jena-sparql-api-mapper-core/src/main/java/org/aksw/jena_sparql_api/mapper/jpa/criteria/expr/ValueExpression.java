package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public class ValueExpression<T>
	extends ExpressionBase<T>
{
	protected T value;

	public ValueExpression(Class<T> javaClass, T value) {
		super(javaClass);
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}
	
//	public static <X> ValueExpression<X> create(X value) {
//		return new ValueExpression<X>(value.getClass(), value);
//	}
}
