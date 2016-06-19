package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.graph.Node;

/**
 * Element for restricting to a certain type.
 *
 * Allows for easy querying by a type.
 * For instance, "lgdo:Airport LIMIT 10" is a valid query.
 *
 * @author raven
 *
 */
public class ElementType
    extends Element0
{
    protected Node type;

    public ElementType(Node type) {
        super();
        this.type = type;
    }

    public Node getType() {
        return type;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
