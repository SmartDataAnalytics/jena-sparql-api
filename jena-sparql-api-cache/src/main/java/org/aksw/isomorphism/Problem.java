package org.aksw.isomorphism;

import java.util.Collection;
import java.util.stream.Stream;



//interface SolutionGeneratorGraph {
//    Stream<S> generateSolution();
//}

/**
 * A problem is an abstract entity that supports generation of (partial) solutions together with an estimated cost of doing so.
 * The cost should thereby be proportional to the number of solutions returned, because:
 * the more solution candidates there are, the more work has to be performed to check them all.   
 * 
 * Usually, a problem is backed by an equivalence class of items,
 * which are the basis for generating solutions.
 * Note, that the framework does not care about the nature of the items and solutions.
 * 
 * @author Claus Stadler
 *
 * @param <S> The solution type
 */
public interface Problem<S>
    extends Comparable<Problem<S>>
{
    long estimateCost();
    Stream<S> generateSolutions();
    Collection<Problem<S>> refine(S partialSolution);
    
    /**
     * By default, compares the estimated costs
     */
    @Override
    default int compareTo(Problem<S> o) {
        long a = o.estimateCost();
        long b = o.estimateCost();
        int result = Long.compare(a, b);
        return result;
    }
}






interface EquivClassGenerator<A, B, S> {
    Collection<Problem<S>> create(Collection<A> a, Collection<B> b);
}
//
//class EquivClassGeneratorImpl<A, B, X, Y, E>
//    implements EquivClassGenerator<A, B, X, Y>
//{
//    protected Function<A, E> aToEquivClass;
//    protected Function<B, E> bToEquivClass;
//
//    @Override
//    public Collection<SolutionGenerator<X, Y>> create(Collection<A> a, Collection<B> b) {
//        Multimap<E, A> aEquivClasses = Iso.indexItemsByEquivClass(a, aToEquivClass);        
//        Multimap<E, B> bEquivClasses = Iso.indexItemsByEquivClass(b, bToEquivClass);
//        
//        Set<E> equivClasses = Sets.union(aEquivClasses.keySet(), bEquivClasses.keySet());
//        equivClasses.parallelStream()
//            .map(e -> {
//                Collection<A> subA = aEquivClasses.get(e);
//                Collection<B> subB = bEquivClasses.get(e);
//                
//            })
//        
//        for(E equivClass : equivClasses) {
//            Collection<M> srcItems = srcEquivClasses.get(equivClass);
//            Collection<N> tgtItems = tgtEquivClasses.get(equivClass);
//
//
//        }
//        
//        // TODO Auto-generated method stub
//        return null;
//    }    
//}

