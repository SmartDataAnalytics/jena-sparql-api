package org.aksw.combinatorics.collections;

import java.util.Arrays;

public class CombinatoricsVector
{
    protected int n;
    protected int[] vector;

    public CombinatoricsVector(int n, int k) {
        this.n = n;
        vector = new int[k];

        for(int i = 0; i < k; ++i) {
            vector[i] = i;
        }
    }

    // The next index on which incrementation will occur
    public int nextIndex(int i) {
        // The maximum value a position may have is (n - (k - i)) = n - k + i

        while(i >= 0 && (vector[i] + 1) > (n - vector.length + i)) {
            --i;
        }

        return i;
    }

    public int nextIndex() {
        int result = nextIndex(vector.length - 1);
        return result;
    }

    /**
     * Request to increment the value at a given index.
     * Can be used to skip all combinations with a certain prefix.
     *
     * @param i
     * @return
     */
    public boolean inc(int i) {
        i = nextIndex(i);

        boolean result = i >= 0;

        if(result) {
            int base = ++vector[i];
            for(int j = i + 1; j < vector.length; ++j) {
                vector[j] = ++base;
            }
        } else {
            vector = null;
        }

        return result;
    }

    public boolean inc() {
        boolean result = inc(vector.length - 1);
        return result;
    }

    public int[] getVector() {
        return vector;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + n;
        result = prime * result + Arrays.hashCode(vector);
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
        CombinatoricsVector other = (CombinatoricsVector) obj;
        if (n != other.n)
            return false;
        if (!Arrays.equals(vector, other.vector))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CombinatoricsVector [n=" + n + ", vector="
                + Arrays.toString(vector) + "]";
    }
}
