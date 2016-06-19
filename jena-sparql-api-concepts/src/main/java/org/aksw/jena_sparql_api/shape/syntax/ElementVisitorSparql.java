package org.aksw.jena_sparql_api.shape.syntax;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.shape.algebra.op.Op;
import org.aksw.jena_sparql_api.shape.algebra.op.OpAnd;
import org.aksw.jena_sparql_api.shape.algebra.op.OpConcept;
import org.aksw.jena_sparql_api.shape.algebra.op.OpExists;
import org.aksw.jena_sparql_api.shape.algebra.op.OpFilter;
import org.aksw.jena_sparql_api.shape.algebra.op.OpFocus;
import org.aksw.jena_sparql_api.shape.algebra.op.OpForAll;
import org.aksw.jena_sparql_api.shape.algebra.op.OpTop;
import org.aksw.jena_sparql_api.shape.algebra.op.OpType;

public class ElementVisitorSparql
    implements ElementVisitor<Op>
{
    @Override
    public OpType visit(ElementType el) {
        OpType result = new OpType(el.getType());
        return result;
    }

    @Override
    public OpConcept visit(ElementSparqlConcept el) {
        OpConcept result = new OpConcept(el.getConcept());
        return result;
    }

    @Override
    public Op visit(ElementDifference el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementUnion el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementValue el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementEnumeration el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementFocus el) {
        return null;
    }

    @Override
    public Op visit(ElementGroup el) {
        List<Element> members = el.getMembers();


//        List<ElementFilter> filters = new ArrayList<>();
//        List<Element> concepts = new ArrayList<>();

        Op result = new OpTop();
        for(Element e : members) {
            if(e instanceof ElementFilter) {
                //filters.add((ElementFilter) e);
                result = new OpFilter(result, ((ElementFilter) e).getExpr());
            } else if(e instanceof ElementFocus) {
                result = new OpFocus(result, ((ElementFocus) e).getPath());
            } else {
                Op x = e.accept(this);
                result = new OpAnd(result, x);
            }
        }


//        Op result = concepts.stream()
//                .map(e -> e.accept(this))
//                .reduce(new OpTop(), (a, b) -> new OpAnd(a, b));
//
//        result = filters.stream()
//                .map(e -> e.getExpr())
//                .reduce(result, (a, b) -> new OpFilter(a, b), (a, b) -> b);

        return result;
    }

    @Override
    public Op visit(ElementAlias el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementBind el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementService el) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op visit(ElementFilter el) {
        Op result = new OpFilter(new OpTop(), el.getExpr());
        return result;
    }

    @Override
    public OpExists visit(ElementExists el) {
        Relation relation = Relation.create(el.getPath());
        Op op = el.getFiller().accept(this);
        OpExists result = new OpExists(relation, op);
        return result;
    }

    @Override
    public Op visit(ElementForAll el) {
        Relation relation = Relation.create(el.getPath());
        Op op = el.getFiller().accept(this);
        OpForAll result = new OpForAll(relation, op);
        return result;
    }

}
