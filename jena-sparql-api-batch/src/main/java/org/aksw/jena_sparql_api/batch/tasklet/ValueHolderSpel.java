package org.aksw.jena_sparql_api.batch.tasklet;

import org.aksw.jena_sparql_api.mapper.util.ValueHolder;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * ValueHolder for a Spring expression language expression
 * 
 * @author raven
 *
 */
public class ValueHolderSpel
    implements ValueHolder
{
    private Expression expr;
    private EvaluationContext context;
    
    public ValueHolderSpel(Expression expr, EvaluationContext context) {
        super();
        this.expr = expr;
        this.context = context;
    }

    @Override
    public Object getValue() {
        Object result = expr.getValue(context);
        return result;
    }

    @Override
    public void setValue(Object value) {
        expr.setValue(context, value);
    }
}
