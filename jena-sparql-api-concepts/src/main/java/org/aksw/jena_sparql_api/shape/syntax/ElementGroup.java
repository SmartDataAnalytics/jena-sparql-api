package org.aksw.jena_sparql_api.shape.syntax;

import java.util.List;

public class ElementGroup
    extends ElementN
{
    public ElementGroup() {
        super();
    }

    public ElementGroup(Element ... members) {
        super(members);
    }

    public ElementGroup(List<Element> members) {
        super(members);
    }


    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
