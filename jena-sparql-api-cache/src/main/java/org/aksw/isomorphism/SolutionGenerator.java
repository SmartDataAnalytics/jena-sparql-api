package org.aksw.isomorphism;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;



//interface SolutionGeneratorGraph {
//    Stream<S> generateSolution();
//}

/**
 * An equivalence class.
 * 
 * 
 * @author raven
 *
 * @param <X>
 * @param <Y>
 */
interface SolutionGenerator<S>
    extends Comparable<SolutionGenerator<S>> // compare by estimated sizes
{
    //Collection<?> exposeLeft(); // Expose the items on the left hand side being matched
    //Collection<?> exposeRight(); // Expose the items on the right hand side being matched
    
    long estimateSize();
    Stream<S> generateSolutions();
    Collection<SolutionGenerator<S>> partition(S partialSolution);
}






interface EquivClassGenerator<A, B, S> {
    Collection<SolutionGenerator<S>> create(Collection<A> a, Collection<B> b);
}

class EquivClassGeneratorImpl<A, B, X, Y, E>
    implements EquivClassGenerator<A, B, X, Y>
{
    protected Function<A, E> aToEquivClass;
    protected Function<B, E> bToEquivClass;

    @Override
    public Collection<SolutionGenerator<X, Y>> create(Collection<A> a, Collection<B> b) {
        Multimap<E, A> aEquivClasses = Iso.indexItemsByEquivClass(a, aToEquivClass);        
        Multimap<E, B> bEquivClasses = Iso.indexItemsByEquivClass(b, bToEquivClass);
        
        Set<E> equivClasses = Sets.union(aEquivClasses.keySet(), bEquivClasses.keySet());
        equivClasses.parallelStream()
            .map(e -> {
                Collection<A> subA = aEquivClasses.get(e);
                Collection<B> subB = bEquivClasses.get(e);
                
            })
        
        for(E equivClass : equivClasses) {
            Collection<M> srcItems = srcEquivClasses.get(equivClass);
            Collection<N> tgtItems = tgtEquivClasses.get(equivClass);


        }
        
        // TODO Auto-generated method stub
        return null;
    }    
}

