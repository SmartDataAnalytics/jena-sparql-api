package org.aksw.isomorphism;

import java.util.AbstractMap.SimpleEntry;

public class Combination2<A, B, S>
    extends SimpleEntry<A, B>
{
    private static final long serialVersionUID = 1L;
    protected S s;

    public Combination2(A a, B b, S s) {
        super(a, b);
        this.s = s;
    }

    public S getS() {
        return s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((s == null) ? 0 : s.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Combination2<?, ?, ?> other = (Combination2<?, ?, ?>) obj;
        if (s == null) {
            if (other.s != null)
                return false;
        } else if (!s.equals(other.s))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "(" + getKey() + ", " + getValue() + "; " + s +")";
    }


}
