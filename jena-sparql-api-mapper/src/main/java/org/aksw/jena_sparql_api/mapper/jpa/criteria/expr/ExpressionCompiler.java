package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.persistence.criteria.Path;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.AggMax;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

/**
 * Compiles JPA criteria expressions to SPARQL expressions.
 * The principle is, that JPA path expressions are compiled to SPARQL graph patterns (Jena's Element),
 * of which have a 'target' variable.
 * The target variable can participate in further expressions.
 *
 *
 *
 * @author raven
 *
 */
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

        if(relation == null) {
            throw new RuntimeException("Could not resolve path: " + e);
        }

        Element element = relation.getElement();
        if(element != null) {
            elements.add(element);
        }

        Expr result = new ExprVar(relation.getTargetVar());
        return result;
    };

    @Override
    public Expr visit(EqualsExpression e) {
        Expr result = appendExpr(e, (a, b) -> new E_Equals(a, b));

        return result;
    }

    @Override
    public Expr visit(GreaterThanExpression e) {
        Expr result = appendExpr(e, (a, b) -> new E_GreaterThan(a, b));

        return result;
    }

    @Override
    public Expr visit(GreaterThanOrEqualToExpression e) {
        Expr result = appendExpr(e, (a, b) -> new E_GreaterThanOrEqual(a, b));

        return result;
    }

    public Expr appendExpr(BinaryOperatorExpression<Boolean> e, BiFunction<Expr, Expr, Expr> op) {
        Expr a = e.getLeftHandOperand().accept(this);
        Expr b = e.getRightHandOperand().accept(this);

        Expr result = op.apply(a, b);
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

        Expr expr = e.getOperand().accept(this);
        AggMax agg = new AggMax(expr);
        Expr result = new ExprAggregator(Vars.x, agg);
        return result;
    }

    @Override
    public Expr visit(AvgExpression e) {
        // Prepare a sub-query

        Expr expr = e.getOperand().accept(this);
        AggAvg agg = new AggAvg(expr);
        Expr result = new ExprAggregator(Vars.x, agg);
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


    @Override
    public Expr visit(LogicalAndExpression e) {
        Expr a = e.getLeftHandOperand().accept(this);
        Expr b = e.getRightHandOperand().accept(this);

        // TODO This way of implementation is certainly wrong - do it right
        return new E_LogicalAnd(a, b);
    }
}
