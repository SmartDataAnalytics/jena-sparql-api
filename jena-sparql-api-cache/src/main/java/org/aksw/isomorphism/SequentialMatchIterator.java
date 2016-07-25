package org.aksw.isomorphism;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

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

    public static <A, B> Iterator<Map<A, B>> create(List<A> as, List<B> bs, Multimap<A, B> mapping) {
        Iterator<Map<A, B>> result = new SequentialMatchIterator<>(as, bs, (a, b) -> mapping.get(a).contains(b));
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
