package org.aksw.jena_sparql_api.shape.syntax;

public class ElementUnion
    extends ElementN
{
    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
