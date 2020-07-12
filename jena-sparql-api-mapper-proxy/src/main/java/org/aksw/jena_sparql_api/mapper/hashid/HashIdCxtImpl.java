package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apache.jena.rdf.model.RDFNode;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

public class HashIdCxtImpl
    implements HashIdCxt
{
//  protected Set<RDFNode> seen;
//  protected Map<RDFNode, HashCode> results;
//    protected RDFNode rdfNode;
    protected HashFunction hashFn;
    protected BiFunction<? super RDFNode, ? super HashIdCxt, ? extends HashCode> globalProcessor;
    protected Set<RDFNode> seen = new LinkedHashSet<>();
    protected Map<RDFNode, HashCode> priorHash = new LinkedHashMap<>();

    protected boolean useInnerIris;
    protected BiPredicate<? super RDFNode, ? super Integer> filterKeep;
    protected int depth;


    public HashIdCxtImpl(
            HashFunction hashFn,
            BiFunction<? super RDFNode, ? super HashIdCxt, ? extends HashCode> globalProcessor)
    {
        this.useInnerIris = false;
        this.filterKeep = (rdfNode, depth) -> true;
        this.hashFn = hashFn;
        this.globalProcessor = globalProcessor;

        this.seen = new LinkedHashSet<>();
    }

    @Override
    public boolean declareVisit(RDFNode node) {
        boolean added = seen.add(node);

        if(!added && !priorHash.containsKey(node)) {
            throw new IllegalStateException("Cyclic dependency; visited this node twice: " + node + " "  + node.getClass());
        }
        return added;
    }

    @Override
    public HashCode putHash(RDFNode node, HashCode hashCode) {
        return priorHash.put(node, hashCode);
    }

    @Override
    public HashCode getHash(RDFNode node) {
        return priorHash.get(node);
    }

    @Override
    public BiFunction<? super RDFNode, ? super HashIdCxt, ? extends HashCode> getGlobalProcessor() {
        return globalProcessor;
    }

    @Override
    public HashFunction getHashFunction() {
        return hashFn;
    }

    @Override
    public Map<RDFNode, HashCode> getMapping() {
        return priorHash;
    }

    @Override
    public boolean isVisited(RDFNode node) {
        boolean result = seen.contains(node);
        return result;
    }
}
