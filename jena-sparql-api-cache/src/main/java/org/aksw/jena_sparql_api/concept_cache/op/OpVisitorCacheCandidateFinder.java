package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Sets;

import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;


// So from the outside we really need candidate mappings
// on the op-name level
// between the cache tree and the query tree
// So this really is sub graph isomorphism,
// but with a little twist that different equivalences hold for different node labels
// e.g. for left joins and disjunctions argument order (the order of the child vertices) matters, whereas
// for e.g. join this is not relevant


/*
 *
 *
 *
 * Essentially, for each node we need to create an iterator of possible child-mappings
 *
 *
 */

class FunctionOpChildren
    implements Function<Op, List<Op>>
{
    @Override
    public List<Op> apply(Op op) {
        List<Op> result = OpUtils.getSubOps(op);
        return result;
    }

    public static final FunctionOpChildren fn = new FunctionOpChildren();
}


class ComparatorOpName
    implements Comparator<Op>
{
    @Override
    public int compare(Op a, Op b) {
        int result = a.getName().compareTo(b.getName());
        return result;
    }

    public static final ComparatorOpName fn = new ComparatorOpName();
}

interface ItFactory<T> {
    Iterator<Map<T, T>> create(T a, T b);
}


/**
 *
 * @author raven
 *
 * @param <T>
 */
class IteratorTreeMatcher<T>
    extends AbstractIterator<Map<T, T>>
{

    /**
     * Comparator to check whether two nodes are equivalent
     * (Maybe jgrapht's equivalence comparator would be appropriate here?)
     */
    public Comparator<T> comparator;

    /**
     * Factory that maps a pair of equivalent nodes
     * to an iterator for all possible matches among their children
     *
     * Probably the factory could have the comparator internally
     */
    ItFactory<Map<Op, Op>> iteratorFactory;


    void dummy() {
        //OpWalker.walk(op, visitor);

    }


    @Override
    protected Map<T, T> computeNext() {
        // TODO Auto-generated method stub
        return null;
    }

}




/**
 * Iterator that requires an exact 1:1 mapping between nodes.
 * So it returns at most a single entry.
 *
 * @author raven
 *
 */
class IteratorMatchDirectMatch<T>
    extends AbstractIterator<Map<T, T>>
{
    public List<T> needle;
    public List<T> haystack;

    public Comparator<T> comparator;

    @Override
    protected Map<T, T> computeNext() {

        int n = needle.size();

        boolean isAllEqual = true;
        // The whole sequence starting at 0 must be equal
        for(int i = 0; i < n; ++i) {
            T needleItem = needle.get(i);
            T haystackItem = haystack.get(i);


            // Check if these items match
            boolean isEqual = comparator.compare(needleItem, haystackItem) == 0;
            if(!isEqual) {
                isAllEqual = false;
                break;
            }
        }

        Map<T, T> result;
        if(isAllEqual) {
            result = new HashMap<T, T>();
            for(int i = 0; i < n; ++i) {
                T needleItem = needle.get(i);
                T haystackItem = haystack.get(i);

                result.put(needleItem, haystackItem);
            }
        } else {
            result = endOfData();
        }

        return result;
    }

}



/**
 *
 * @author raven
 *
 */
interface CacheOpSuplier {
    Op getCandidates(Op prototype);
}


/**
 * Matches nodes of two OP trees
 *
 * @author raven
 *
 */
class MatcherState {
    private Map<Op, Op> needleToHaystack;

    /**
     * Leafs are the set of nodes of the needle that were not mapped yet
     */
    private Set<Op> leafs = Sets.newIdentityHashSet();



}




class OpMatcher {
    private Op currentOp;

    public OpMatcher(OpMatcher parent) {

    }

    public Op getCurrentOp() {
        return currentOp;
    }

    public OpMatcher progress(int argIndex, Op cacheOp) {
        return null;
    }
}




/**
 * Walks a query structure and at each node checks
 *
 * @author raven
 *
 */
public class OpVisitorCacheCandidateFinder
    extends OpVisitorBase
{
    private Collection<Op> availableOps;

    public <T> Collection<OpMatcher> initiate(Class<T> clazz) {

        return null;
    }

    /**
     * Find the set of ops that
     */
    public <T extends Op> Collection<T> find(Class<T> clazz) {
        Set<T> result = Sets.newIdentityHashSet();

        for(Op candidate : availableOps) {
            boolean isClassValid = candidate.getClass().isAssignableFrom(clazz);
            //if(candidate.getName().equals(op.getName())) {

            if(isClassValid) {
                result.add((T)candidate);
            }
        }

        return result;
    }


    @Override
    public void visit(OpExt op) {
        if(op instanceof OpExtConjunctiveQuery) {
            this.visit((OpExtConjunctiveQuery) op);
        }
    }

    public void visit(OpDistinct op) {
        // TODO Initiation should return a set of initial mappings,
        // which can then be continued with further iteration of the query
        // the result of this visitor is then the set of OpMappings that were traversed until the end
        //
        // wh

        //Collection<OpDistinct> initiated = initiate(OpDistinct.class);

        //


        op.visit(this);
    }

    public void visit(OpExtConjunctiveQuery op) {
        //op.visit(opVisitor);
    }

}

