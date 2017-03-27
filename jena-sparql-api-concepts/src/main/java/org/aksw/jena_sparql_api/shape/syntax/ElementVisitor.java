package org.aksw.jena_sparql_api.shape.syntax;

public interface ElementVisitor<T> {
    public T visit(ElementType el);
    public T visit(ElementSparqlConcept el);
    public T visit(ElementDifference el);
    public T visit(ElementUnion el);
    public T visit(ElementValue el);
    public T visit(ElementEnumeration el);
    public T visit(ElementFocus el);
    public T visit(ElementGroup el);
    public T visit(ElementAlias el);
    public T visit(ElementBind el);
    public T visit(ElementService el);
    public T visit(ElementFilter el);
    public T visit(ElementExists el);
    public T visit(ElementForAll el);
}
