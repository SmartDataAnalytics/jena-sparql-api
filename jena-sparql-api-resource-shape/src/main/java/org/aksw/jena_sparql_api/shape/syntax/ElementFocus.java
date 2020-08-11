package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.Path;

/**
 * Navigates from one set of resources to a related one via the given path
 * @author raven
 *
 */
public class ElementFocus
    extends Element0
{
    protected Path path;

    public ElementFocus(Path path) {
        //super(subElement);
        super();
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
