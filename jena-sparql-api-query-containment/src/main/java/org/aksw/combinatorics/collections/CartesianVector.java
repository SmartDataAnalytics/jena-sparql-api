package org.aksw.combinatorics.collections;

public class CartesianVector
{
    private int n;
    private int[] vector;

    public CartesianVector(int n, int k) {
        this.n = n;
        vector = new int[k];

        for(int i = 0; i < k; ++i) {
            vector[i] = 0;
        }
    }

    // The next index on which incrementation will occur
    public int nextIndex(int i) {
        // The maximum value a position may have is n -1
        while(i >= 0 && (vector[i] + 1) > (n - 1)) {
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
     *
     * @param i
     * @return
     */
    public boolean inc(int i) {
        i = nextIndex(i);

        boolean result = i >= 0;

        if(result) {
            ++vector[i];
            for(int j = i + 1; j < vector.length; ++j) {
                vector[j] = 0;
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

    public int[] vector() {
        return vector;
    }
}
