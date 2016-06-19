package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.core.Var;

/**
 * Name an element with a variable
 *
 * @author raven
 *
 */
public class ElementAlias
    extends Element1
{
    protected Var var;

    public ElementAlias(Element subElement, Var var) {
        super(subElement);
        this.var = var;
    }

    public Var getVar() {
        return var;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
