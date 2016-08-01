package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;

import com.google.common.math.LongMath;

/**
 * Base class for mapping problems where *every* item of a set 'as'
 * must *uniquely* match an item in 'bs'.
 *
 * @author raven
 *
 * @param <S>
 * @param <A>
 * @param <B>
 */
public abstract class ProblemMappingKPermutationsOfN<A, B, X, Y>
    implements ProblemNeighborhoodAware<Map<X, Y>, X>
{
    protected Collection<A> as;
    protected Collection<B> bs;
    protected Map<X, Y> baseSolution;

    public ProblemMappingKPermutationsOfN(Collection<A> as, Collection<B> bs, Map<X, Y> baseSolution) {
        super();
        this.as = as;
        this.bs = bs;
        this.baseSolution = baseSolution;

        if(this.baseSolution == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public long getEstimatedCost() {
        int k = as.size();
        int n = bs.size();

        long result;
        if(k > n) {
            result = 0;
        } else {
          //result = LongMath.factorial(n) / LongMath.factorial(n - k);

//	The formula below should be equal to the one above
            long combinationCount = LongMath.binomial(n, k);
            long permutationCount = LongMath.factorial(k);
            result = combinationCount * permutationCount;
        }

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((as == null) ? 0 : as.hashCode());
        result = prime * result + ((baseSolution == null) ? 0 : baseSolution.hashCode());
        result = prime * result + ((bs == null) ? 0 : bs.hashCode());
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
        ProblemMappingKPermutationsOfN<?, ?, ?, ?> other = (ProblemMappingKPermutationsOfN<?, ?, ?, ?>) obj;
        if (as == null) {
            if (other.as != null)
                return false;
        } else if (!as.equals(other.as))
            return false;
        if (baseSolution == null) {
            if (other.baseSolution != null)
                return false;
        } else if (!baseSolution.equals(other.baseSolution))
            return false;
        if (bs == null) {
            if (other.bs != null)
                return false;
        } else if (!bs.equals(other.bs))
            return false;
        return true;
    }

    @Override
    public Collection<ProblemNeighborhoodAware<Map<X, Y>, X>> refine(Map<X, Y> partialSolution) {
        return Collections.singleton(this);
    }

    @Override
    public String toString() {
        return "ProblemMappingEquivBase [as=" + as + ", bs=" + bs
                + ", baseSolution=" + baseSolution + "]";
    }


//  public static Collection<Problem<Map<Var, Var>>> createProblems(    protected Entry<? extends Collection<Quad>, ? extends Collection<Quad>> quadGroup;
//) {
//
//  }

}
