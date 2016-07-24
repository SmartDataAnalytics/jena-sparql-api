package org.aksw.isomorphism;

import java.util.List;

import com.google.common.collect.Multimap;

/**
 * Given (1) a multimap of candidate mappings a and b and (2) two lists of x and y
 * find all possbile sequences of x mapping to ys 
 * 
 * - test if the src and tgt set of the mapping are consecutive according to xy and y.
 * 
 * @author raven
 *
 */
public class LinearCandidateLists<A, B, S> {
    //protected BiHashMultimap<A, B> remaining;
    protected List<A> aOrder;
    protected List<B> bOrder;
    protected Multimap<A, B> mapping;

}
