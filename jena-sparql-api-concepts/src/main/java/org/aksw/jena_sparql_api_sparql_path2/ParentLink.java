package org.aksw.jena_sparql_api_sparql_path2;

import java.io.Serializable;

public class ParentLink<V, E>
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -8669731014620534012L;

    protected NestedPath<V, E> target;
    protected DirectedProperty<E> diProperty;

    public ParentLink(DirectedProperty<E> diProperty) {
        this(null, diProperty);
    }

    public ParentLink(NestedPath<V, E> target, DirectedProperty<E> diProperty) {
        super();
        this.target = target;
        this.diProperty = diProperty;
    }

    public NestedPath<V, E> getTarget() {
        return target;
    }

    public DirectedProperty<E> getDiProperty() {
        return diProperty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((diProperty == null) ? 0 : diProperty.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParentLink other = (ParentLink) obj;
        if (diProperty == null) {
            if (other.diProperty != null)
                return false;
        } else if (!diProperty.equals(other.diProperty))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ParentLink [target=" + target + ", diProperty=" + diProperty
                + "]";
    }

}
