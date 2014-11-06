package org.aksw.jena_sparql_api.concept_cache;

public class CombinatoricsVector
{
    private int n;
    private int[] vector;

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

    public int[] vector() {
        return vector;
    }
}
