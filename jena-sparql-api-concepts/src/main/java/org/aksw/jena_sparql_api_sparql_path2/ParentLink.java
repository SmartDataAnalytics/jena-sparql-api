package org.aksw.jena_sparql_api_sparql_path2;

public class ParentLink<V, E> {
    protected NestedPath<V, E> target;
    protected DirectedProperty<E> diProperty;

    public ParentLink(DirectedProperty<E> diProperty) {
        this(null, diProperty);
    }

    public ParentLink(NestedPath<V, E> target, DirectedProperty<E> diProperty) {
        super();
        this.diProperty = diProperty;
    }

    public NestedPath<V, E> getTarget() {
        return target;
    }

    public DirectedProperty<E> getDiProperty() {
        return diProperty;
    }


}
