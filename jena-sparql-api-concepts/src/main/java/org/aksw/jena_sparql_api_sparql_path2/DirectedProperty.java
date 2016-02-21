package org.aksw.jena_sparql_api_sparql_path2;

import java.io.Serializable;

public class DirectedProperty<E>
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 4826688961505056387L;

    protected E property;
    protected boolean isReverse;

    public DirectedProperty(E property) {
        this(property, false);
    }

    public DirectedProperty(E property, boolean isReverse) {
        super();
        this.property = property;
        this.isReverse = isReverse;
    }

    public E getProperty() {
        return property;
    }

    public boolean isReverse() {
        return isReverse;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isReverse ? 1231 : 1237);
        result = prime * result
                + ((property == null) ? 0 : property.hashCode());
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
        DirectedProperty<?> other = (DirectedProperty<?>) obj;
        if (isReverse != other.isReverse)
            return false;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DirectedProperty [property=" + property + ", isReverse="
                + isReverse + "]";
    }

}
