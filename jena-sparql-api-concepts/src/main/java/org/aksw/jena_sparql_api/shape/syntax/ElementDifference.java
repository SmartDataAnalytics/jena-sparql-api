package org.aksw.jena_sparql_api.shape.syntax;

public class ElementDifference
    extends Element1
{
    public ElementDifference(Element subElement) {
        super(subElement);
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
