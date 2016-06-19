package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.Path;

/**
 * Navigates from one set of resources to a related one via the given path
 * @author raven
 *
 */
public class ElementFocus
    extends Element1
{
    protected Path path;

    public ElementFocus(Element subElement, Path path) {
        super(subElement);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
