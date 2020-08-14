package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.jena.rdf.model.RDFNode;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.BaseEncoding;

public class HashIdCxtImpl
    implements HashIdCxt
{
//  protected Set<RDFNode> seen;
//  protected Map<RDFNode, HashCode> results;
//    protected RDFNode rdfNode;
    protected HashFunction hashFn;
    protected BiFunction<? super RDFNode, ? super HashIdCxt, ? extends HashCode> globalProcessor;

    /** The set of items for which processing was requested (items are thus in processing or processed state) */
    protected Set<RDFNode> processing = new LinkedHashSet<>();

    /** The set of reachable items not yet in processing */
    protected Set<RDFNode> pending = new LinkedHashSet<>();

    /** Items that are fully processed have an entry of the processing result in this map */
    protected Map<RDFNode, HashCode> rdfNodeToHashCode = new LinkedHashMap<>();

    /** The set of items in traversed state. */
//    protected Set<RDFNode> traversals = new LinkedHashSet<>();

    protected Map<RDFNode, String> rdfNodeToString = new LinkedHashMap<>();

    protected Function<HashCode, String> hashCodeEncoder;

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
        this.hashCodeEncoder = hashCode -> BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        // TODO do we need null handling?
        //        this.hashCodeEncoder = hashCode -> hashCode == null ? "(null)" : BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());

        this.processing = new LinkedHashSet<>();
    }

    /**
     * Declaration of processing implies a call to declareTraversal.
     * Conversely, it is possible to traverse through nodes without triggering processing.
     *
     */
    @Override
    public boolean declareProcessing(RDFNode node) {
        boolean added = processing.add(node);

        if(!added && !rdfNodeToHashCode.containsKey(node)) {
            throw new IllegalStateException("Cyclic dependency; visited this node twice: " + node.asResource() + " "  + node.getClass());
        }

        pending.remove(node);

//        declareTraversal(node);

        return added;
    }

//    @Override
//    public boolean declareTraversal(RDFNode node) {
//        boolean added = traversals.add(node);
//        return added;
//    }

    @Override
    public HashCode putHashId(RDFNode node, HashCode hashCode) {
        return rdfNodeToHashCode.put(node, hashCode);
    }

    @Override
    public HashCode getHashId(RDFNode node) {
        return rdfNodeToHashCode.get(node);
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
    public Map<RDFNode, HashCode> getHashIdMapping() {
        return rdfNodeToHashCode;
    }

    @Override
    public boolean isVisited(RDFNode node) {
        boolean result = processing.contains(node);
        return result;
    }

    @Override
    public String getStringId(RDFNode node) {
        return rdfNodeToString.get(node);
    }

    @Override
    public String putStringId(RDFNode node, String str) {
        return rdfNodeToString.put(node, str);
    }

    @Override
    public Map<RDFNode, String> getStringIdMapping() {
        return rdfNodeToString;
    }


    @Override
    public String getHashAsString(HashCode hashCode) {
        Objects.requireNonNull(hashCode, "hashCode should not be null here");
        String result = hashCodeEncoder.apply(hashCode);
        return result;
    }


    // TODO Make it a default method in the interface?
    @Override
    public String getHashAsString(RDFNode rdfNode) {
        HashCode hashCode = getHashId(rdfNode);
        String result = hashCode == null ? null : getHashAsString(hashCode);
        return result;
    }

    @Override
    public boolean declarePending(RDFNode node) {
        boolean result = pending.add(node);
        return result;
    }

    @Override
    public boolean isPending(RDFNode node) {
        return pending.contains(node);
    }

    @Override
    public Set<RDFNode> getPending() {
        return pending;
    }
}
