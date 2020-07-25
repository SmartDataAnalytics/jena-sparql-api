package org.aksw.jena_sparql_api.mapper.hashid;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


public class ClassDescriptor {
    protected Class<?> clazz;
    protected Map<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>> rawPropertyProcessors = new LinkedHashMap<>();

    protected Set<P_Path0> hashIdPaths = new LinkedHashSet<>();
    //protected Map<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>>

    protected Set<BiFunction<? super Resource, ? super HashIdCxt, ? extends HashCode>> directHashIdProcessors = new LinkedHashSet<>();

    protected Set<BiFunction<? super Resource, ? super HashIdCxt, ? extends String>> directStringIdProcessors = new LinkedHashSet<>();


    public ClassDescriptor(Class<?> clazz) {
        super();
        this.clazz = clazz;
    }

    // iri to method name to effective type to getter/setter
    // protected Table<String, String, Map<SimpleType, MethodGroup>> iriToNameToTypeToGroup;//  = new LinkedHashMap<>();

    public void registerRawAccessor(P_Path0 path, boolean isHashId, Function<Resource, Collection<? extends RDFNode>> processor) {
        rawPropertyProcessors.put(path, processor);

        if(isHashId) {
            hashIdPaths.add(path);
        }
    }


    public Map<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>> getRawPropertyProcessors() {
        return rawPropertyProcessors;
        // return Maps.filterKeys(rawPropertyProcessors, hashIdPaths::contains);
    }


    public void registerDirectHashIdProcessor(BiFunction<? super Resource, ? super HashIdCxt, ? extends HashCode> processor) {
        directHashIdProcessors.add(processor);
    }

    public void registerDirectStringIdProcessor(BiFunction<? super Resource, ? super HashIdCxt, ? extends String> processor) {
        directStringIdProcessors.add(processor);
    }


//    public HashCode computHashId(Resource node, HashIdCxt cxt) {
//        cxt.declareVisit(node);
//
//        HashCode hashCode = worker.apply(node, cxt);
//
//        cxt.putHash(node, hashCode);
//        return hashCode;
//    }

    public HashCode computeHashId(Resource node, HashIdCxt cxt) {
//        cxt.declareVisit(node);

        HashFunction hashFn = cxt.getHashFunction();

        Map<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>> rawPropertyProcessors = getRawPropertyProcessors();


        Map<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>>
            hashIdProcessors = Maps.filterKeys(rawPropertyProcessors, hashIdPaths::contains);


        HashCode result;
        if(!hashIdProcessors.isEmpty() || !directHashIdProcessors.isEmpty()) {
            List<HashCode> hashes = new ArrayList<>();
            for(Entry<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>> e : hashIdProcessors.entrySet()) {
                P_Path0 path = e.getKey();
    //            System.err.println("Computing id via " + path);

                String iri = path.getNode().getURI();
                boolean isFwd = path.isForward();
                Function<? super Resource, ? extends Collection<? extends RDFNode>> propertyAccessor = e.getValue();

                Collection<? extends RDFNode> col = propertyAccessor.apply(node);
                Class<?> colClass = col.getClass();

                boolean isOrdered = List.class.isAssignableFrom(colClass);

                List<HashCode> hashContribs = new ArrayList<>();
                for(RDFNode item : col) {
                    System.err.println("Gathering hashId contrib from " + clazz.getCanonicalName() + "." + path + " from " + ResourceUtils.asBasicRdfNode(node) + " to " + ResourceUtils.asBasicRdfNode(item));

                    HashCode partialHashContrib = cxt.getGlobalProcessor().apply(item, cxt);

                    if(partialHashContrib == null) {
                        throw new NullPointerException();
                    }

                    // Note that here we repeatedly compute the hash of the property
                    // We may want to factor this out
                    HashCode fullHashContrib = hashFn.newHasher()
                        .putString(iri, StandardCharsets.UTF_8)
                        .putBoolean(isFwd)
                        .putBytes(partialHashContrib.asBytes())
                        .hash();

                    hashContribs.add(fullHashContrib);
                }


                HashCode propertyHash = hashContribs.isEmpty()
                        ? hashFn.hashInt(0)
                        : isOrdered
                            ? Hashing.combineOrdered(hashContribs)
                            : Hashing.combineUnordered(hashContribs);

                hashes.add(propertyHash);
            }

            for(BiFunction<? super Resource, ? super HashIdCxt, ? extends HashCode> directHashIdProcessor : directHashIdProcessors) {
                HashCode contrib = directHashIdProcessor.apply(node, cxt);
                hashes.add(contrib);
            }

            if(hashes.isEmpty()) {
                throw new RuntimeException("Could not obtain ID hashes for " + clazz.getCanonicalName() + " with node " + node);
            }

            result = Hashing.combineUnordered(hashes);

            // TODO HACK Ideally this code should not have to rely on registering hashes manually
            // But right now the subsequent depth-first-traversal for computing ids of all reachable
            // resources requires this kind of handling
            // The alternative would be to register all reachable non-hashid nodes to the cxt
            // and let the outer procedure recurse over it
            cxt.putHash(node, result);
            for(BiFunction<? super Resource, ? super HashIdCxt, ? extends String> e : directStringIdProcessors) {
                String str = e.apply(node, cxt);
                cxt.putString(node, str);
            }
        } else {
            result = null;
            if(cxt.getHash(node) == null) {
                cxt.putHash(node, null);
            }
        }



//        cxt.putHash(node, result);

        return result;
    }

    public void collectReachableResources(Resource node, HashIdCxt cxt) {

//        Map<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>>
//            nonHashIdProcessors = Maps.filterKeys(rawPropertyProcessors, x -> !hashIdPaths.contains(x));


        for(Entry<P_Path0, Function<? super Resource, ? extends Collection<? extends RDFNode>>> e : rawPropertyProcessors.entrySet()) {
            P_Path0 path = e.getKey();
//            System.err.println("Scanning " + path);
            Function<? super Resource, ? extends Collection<? extends RDFNode>> propertyAccessor = e.getValue();
            Collection<? extends RDFNode> col = propertyAccessor.apply(node);

            for(RDFNode rdfNode : col) {
                try {
                    if(!cxt.isPending(rdfNode)) {
//                        System.err.println("Traversing from " + node.asResource() + " (" + clazz + ") to " + rdfNode.asResource() + " via " + path);
                        System.err.println("Traversal " + clazz.getCanonicalName() + "." + path + " from " + ResourceUtils.asBasicRdfNode(node) + " to " + ResourceUtils.asBasicRdfNode(rdfNode));
                        // cxt.getGlobalProcessor().apply(rdfNode, cxt);
                        MapperProxyUtils.collectReachableResources(rdfNode, cxt);
                    }
                } catch(IllegalStateException ex) {
                    throw ex;
                } catch(Exception ex) {
//                    System.err.println("Failed to scan " + path + " " + ex);
                    // TODO Silently catching the exception here is bad
                }
            }
        }
    }
}

