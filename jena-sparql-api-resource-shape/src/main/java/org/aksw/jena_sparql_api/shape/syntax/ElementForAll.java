package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.Path;

public class ElementForAll
    extends ElementPathConstraint
{
    public ElementForAll(Path path, Element filler) {
        super(path, filler);
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
