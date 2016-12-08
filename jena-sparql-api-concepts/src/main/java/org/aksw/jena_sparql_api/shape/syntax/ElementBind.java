package org.aksw.jena_sparql_api.shape.syntax;

// TODO Not sure if this binds an sparql expression or a concept element to a variable
public class ElementBind
    extends Element0
{
    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
