package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Multimap;

//class MappingSolution<A, B, S> {
//    protected Multimap<A, B> mapping;
//    //protected A
//}

/**
 * Elements in aOrder and bOrder must be unique.
 * 
 * @author raven
 *
 * aOrder.size() must be less-than-or-equal to bOrder.size()
 *
 * @param <A>
 * @param <B>
 * @param <S>
 */
public class SequentialMatchIterator<A, B, S>
    extends AbstractIterator<Map<A, B>>
{
    //protected BiHashMultimap<A, B> remaining;
    protected List<A> aOrder;
    protected List<B> bOrder;
    //protected Multimap<A, B> mapping;
    protected BiPredicate<A, B> isMatch;
    
    protected int i = 0;

    /**
     * The cost of iterating all possible solutions.
     * The actual number of solutions may be significantly less.
     * 
     * @return
     */
    public int estimateCost() {
        int m = aOrder.size();
        int n = bOrder.size();
        int result = m * n;
        return result;
    }
    
    public SequentialMatchIterator(List<A> aOrder, List<B> bOrder, BiPredicate<A, B> isMatch) {
        super();
        this.aOrder = aOrder;
        this.bOrder = bOrder;
        //this.mapping = mapping;
        this.isMatch = isMatch;
    }

    @Override
    protected Map<A, B> computeNext() {
        int aSize = aOrder.size();
        int bSize = bOrder.size();
        int d = bSize - aSize;
        
        Map<A, B> result = null;
        

        boolean foundCompleteMatch = false;
        for(; i <= d; ++i) {
            boolean foundContribution = true;
            for(int j = 0; j < aSize; ++j) {                
                A a = aOrder.get(j);
                B b = bOrder.get(i + j);
                foundContribution = isMatch.test(a, b);
                
                if(!foundContribution) {
                    foundCompleteMatch = false;
                    break;
                }                
            }
            
            if(foundContribution) {
                foundCompleteMatch = true;
                result = new LinkedHashMap<>();
                for(int j = 0; j < aSize; ++j) {                
                    A a = aOrder.get(j);
                    B b = bOrder.get(i + j);
                    result.put(a, b);
                }
                break;
            }
        }

        ++i;

        if(!foundCompleteMatch) {

            
            
            
            
            
            
            result = endOfData();
        }
        
        
        return result;
    }

    public static <A, B> Iterable<Map<A, B>> createIterable(List<A> as, List<B> bs, Multimap<A, B> mapping) {
        Iterable<Map<A, B>> result = () -> new SequentialMatchIterator<>(as, bs, (a, b) -> mapping.get(a).contains(b));
        //IterableUnknownSize<Map<A, B>> result = new IterableUnknownSizeSimple<>(tmp);
        //Optional<Iterable<Map<A, B>>> result = Optional.of(tmp);
        //Stream<Map<A, B>> result = StreamSupport.stream(it.spliterator(), false);
        
        return result;
    }

    public static <A, B> Stream<Map<A, B>> createStream(List<A> as, List<B> bs, Multimap<A, B> mapping) {
        Iterable<Map<A, B>> it = () -> new SequentialMatchIterator<>(as, bs, (a, b) -> mapping.get(a).contains(b));
        Stream<Map<A, B>> result = StreamSupport.stream(it.spliterator(), false);
        
        return result;
    }
}


/**
 * Given (1) a multimap of candidate mappings a and b and (2) two lists of x and y
 * find all possbile sequences of x mapping to ys 
 * 
 * - test if the src and tgt set of the mapping are consecutive according to xy and y.
 * 
 * @author raven
 *
 */
//public class LinearCandidateLists<A, B>
//    implements Iterable<Map<A, B>>
//{
//    //protected BiHashMultimap<A, B> remaining;
//    protected List<A> aOrder;
//    protected List<B> bOrder;
//    protected Multimap<A, B> mapping;
//    
//    }
//}
