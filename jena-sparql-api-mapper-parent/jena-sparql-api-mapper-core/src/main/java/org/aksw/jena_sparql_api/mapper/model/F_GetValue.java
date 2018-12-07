package org.aksw.jena_sparql_api.mapper.model;

import java.util.function.Function;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;


public class F_GetValue<T>
    implements Function<Object, T>
{
    protected Class<T> clazz;
    protected Expression expression;
    protected EvaluationContext evalContext;

    public F_GetValue(Class<T> valueClazz, Expression expression,
            EvaluationContext evalContext) {
        super();
        this.clazz = valueClazz;
        this.expression = expression;
        this.evalContext = evalContext;
    }

    @Override
    public T apply(Object arg) {
        T result = expression.getValue(evalContext, arg, clazz);
        return result;
    }
}
