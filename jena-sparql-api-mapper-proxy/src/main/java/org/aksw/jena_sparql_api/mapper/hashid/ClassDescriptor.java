package org.aksw.jena_sparql_api.mapper.hashid;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


public class ClassDescriptor {
    protected Class<?> clazz;
    protected Map<P_Path0, Function<Resource, Collection<? extends RDFNode>>> rawPropertyProcessors = new LinkedHashMap<>();

    public ClassDescriptor(Class<?> clazz) {
        super();
        this.clazz = clazz;
    }

    // iri to method name to effective type to getter/setter
    // protected Table<String, String, Map<SimpleType, MethodGroup>> iriToNameToTypeToGroup;//  = new LinkedHashMap<>();

    public void registerRawAccessor(P_Path0 path, Function<Resource, Collection<? extends RDFNode>> processor) {
        rawPropertyProcessors.put(path, processor);
    }


    public Map<P_Path0, Function<Resource, Collection<? extends RDFNode>>> getHashIdProcessors() {
        return rawPropertyProcessors;
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

        Map<P_Path0, Function<Resource, Collection<? extends RDFNode>>> rawPropertyProcessors = getHashIdProcessors();

        List<HashCode> hashes = new ArrayList<>();
        for(Entry<P_Path0, Function<Resource, Collection<? extends RDFNode>>> e : rawPropertyProcessors.entrySet()) {
            P_Path0 path = e.getKey();
            String iri = path.getNode().getURI();
            boolean isFwd = path.isForward();
            Function<Resource, Collection<? extends RDFNode>> propertyAccessor = e.getValue();

            Collection<? extends RDFNode> col = propertyAccessor.apply(node);
            Class<?> colClass = col.getClass();

            boolean isOrdered = List.class.isAssignableFrom(colClass);

            List<HashCode> hashContribs = new ArrayList<>();
            for(RDFNode item : col) {
                HashCode partialHashContrib = cxt.getGlobalProcessor().apply(item, cxt);

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

        HashCode result = Hashing.combineUnordered(hashes);
//        cxt.putHash(node, result);

        return result;
    }
}

