package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Map;

import org.aksw.isomorphism.Problem;

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
public abstract class ProblemMappingEquivBase<A, B, X, Y>
    implements Problem<Map<X, Y>>
{
    protected Collection<A> as;
    protected Collection<B> bs;
    protected Map<X, Y> baseSolution;

    public ProblemMappingEquivBase(Collection<A> as, Collection<B> bs, Map<X, Y> baseSolution) {
        super();
        this.as = as;
        this.bs = bs;
    }

    @Override
    public long getEstimatedCost() {
        int k = as.size();
        int n = bs.size();
        
        long combinationCount = LongMath.binomial(n, k);
        long permutationCount = LongMath.factorial(k); 
        long result = combinationCount * permutationCount;
        
        return result;
    }
    
    
    
//  public static Collection<Problem<Map<Var, Var>>> createProblems(    protected Entry<? extends Collection<Quad>, ? extends Collection<Quad>> quadGroup;
//) {
//  
//  }

}
