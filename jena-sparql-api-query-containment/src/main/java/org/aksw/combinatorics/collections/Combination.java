package org.aksw.combinatorics.collections;

import java.util.AbstractMap.SimpleEntry;

/**
 * TODO Replace with guava's Cell class
 *
 * @author raven
 *
 * @param <A>
 * @param <B>
 * @param <S>
 */
public class Combination<A, B, S>
    extends SimpleEntry<A, B>
{
    private static final long serialVersionUID = 1L;
    protected S solution;

    public Combination(A a, B b, S solution) {
        super(a, b);
        this.solution = solution;
    }

    public S getSolution() {
        return solution;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((solution == null) ? 0 : solution.hashCode());
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
        Combination<?, ?, ?> other = (Combination<?, ?, ?>) obj;
        if (solution == null) {
            if (other.solution != null)
                return false;
        } else if (!solution.equals(other.solution))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "(" + getKey() + ", " + getValue() + "; " + solution +")";
    }


}
