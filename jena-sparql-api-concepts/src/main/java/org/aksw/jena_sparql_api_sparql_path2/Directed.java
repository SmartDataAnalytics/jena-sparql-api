package org.aksw.jena_sparql_api_sparql_path2;

import java.io.Serializable;

public class Directed<E>
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 4826688961505056387L;

    protected E value;
    protected boolean isReverse;

    public Directed(E value) {
        this(value, false);
    }

    public Directed(E value, boolean isReverse) {
        super();
        this.value = value;
        this.isReverse = isReverse;
    }

    public E getValue() {
        return value;
    }

    public boolean isForward() {
        return !isReverse;
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
                + ((value == null) ? 0 : value.hashCode());
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
        Directed<?> other = (Directed<?>) obj;
        if (isReverse != other.isReverse)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Directed [value=" + value + ", isReverse="
                + isReverse + "]";
    }

}
