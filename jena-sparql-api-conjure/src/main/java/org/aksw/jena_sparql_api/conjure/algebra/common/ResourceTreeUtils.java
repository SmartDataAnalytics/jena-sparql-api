package org.aksw.jena_sparql_api.conjure.algebra.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.graph.SuccessorsFunction;
import org.apache.jena.ext.com.google.common.graph.Traverser;
import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.ext.com.google.common.hash.HashFunction;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.util.iterator.ExtendedIterator;


public class ResourceTreeUtils {

    public static <T> Iterable<? extends T> peekingSubOps(T op, SuccessorsFunction<T> successorsFunction) {
        Iterable<? extends T> result = successorsFunction.successors(op);
        System.out.println("SubOps for " + op.getClass() + " "+ op + " are " + result);
        return result;
    }

    public static <T> int getNumOps(T op, SuccessorsFunction<T> successorsFunction) {

        // TODO We may want to exclude counting leaf nodes
        // as they do not require any operation
        // then again, a node with multiple children may require more time
        // than one with fewer
        /**
         * Get the number of operations in the expression.
         * Can be used as a poor-mans cost estimate
         */
//		int result = (int)Streams.stream(Traverser.forTree(OpUtils::peekingSubOps)
        int result = (int)Streams.stream(Traverser.forTree(successorsFunction)
            .depthFirstPreOrder(op))
            .count();

        return result;
    }

    /**
     * Recursively delete all properties of all resources reachable from the root resource
     * (inclusive) in outgoing direction.
     * The resource itself is not deleted.
     *
     *
     * @param root
     */
    public static void clearSubTree(Resource root) {
        clearSubTree(root, r -> ResourceUtils.listPropertyValues(r, null, Resource.class).toSet());
    }

    public static <T extends Resource> void clearSubTree(T rootOp, SuccessorsFunction<T> successorsFunction) {
        List<T> ops = Streams.stream(Traverser.forTree(successorsFunction)
                .depthFirstPostOrder(rootOp))
                .collect(Collectors.toList());

        for(T op : ops) {
            // TODO Handle lists
            op.removeProperties();
        }
    }


    public static HashCode generateModelHash(Model model, HashFunction hashFunction) {
        Graph graph = model.getGraph();
        HashCode result = generateModelHash(graph, hashFunction);
        return result;
    }

    public static HashCode generateModelHash(Graph graph, HashFunction hashFunction) {
        HashCode result = hashFunction.hashString("# begin of ntriples", StandardCharsets.UTF_8);
        ExtendedIterator<Triple> it = graph.find();
        try {
            while(it.hasNext()) {
                //NodeFmtLib.str(t)
                Triple t = it.next();
                StringBuilder sb = new StringBuilder();
                sb.append(t.getSubject().isBlank() ? "_:" : t.getSubject());
                sb.append(" ");
                sb.append(t.getPredicate());
                sb.append(" ");
                sb.append(t.getObject().isBlank() ? "_:" : t.getObject());

                HashCode tmp = hashFunction.hashString(sb.toString(), StandardCharsets.UTF_8);
                result = Hashing.combineUnordered(Arrays.asList(result, tmp));
            }
        } finally {
            it.close();
        }
        return result;
    }

    /**
     * Create a hash for a resource based on transitively following its
     * outgoing properties
     *
     *
     * For each of a reosurce's statement, first a hash for its object is recursively computed.
     * Afterwards, the so obtained (property, hash) pairs are then combined into an overall hash using
     * Hashing.combineUnordered.
     *
     * Note, that URIs of nodes are NOT considered unless they are leaf nodes.
     *
     * ISSUE We need to be able to 'skip' nodes
     *
     * @param rdfNode
     * @return
     */
    public static HashCode createGenericHash(RDFNode rdfNode, boolean useInnerIris) {
        Set<RDFNode> seen = new HashSet<>();
        Map<RDFNode, HashCode> priorHash = new HashMap<>();
        HashCode result = createGenericHash(rdfNode, useInnerIris, (n, d) -> true, 0, seen, Hashing.sha256(), priorHash);
        return result;
    }

    public static HashCode createGenericHash(RDFNode rdfNode) {
        return createGenericHash(rdfNode, false);
    }

    public static Map<RDFNode, HashCode> createGenericHashMap(RDFNode rdfNode, boolean useInnerIris, BiPredicate<? super RDFNode, ? super Integer> filterKeep) {
        Set<RDFNode> seen = new HashSet<>();
        Map<RDFNode, HashCode> result = new HashMap<>();
        createGenericHash(rdfNode, useInnerIris, filterKeep, 0, seen, Hashing.sha256(), result);
        return result;
    }

    public static Map<RDFNode, HashCode> createGenericHashMap(RDFNode rdfNode, boolean useInnerIris) {
        Map<RDFNode, HashCode> result = createGenericHashMap(rdfNode, useInnerIris, (n, d) -> true);
        return result;
    }

//    public static HashCode createGenericHash2(RDFNode rdfNode) {
//        String str = createGenericHash(rdfNode);
//        HashCode result = Hashing.sha256().hashString(str, StandardCharsets.UTF_8);
//        return result;
//    }



    /**
     * Computes hashes for nodes of an tree structured RDF graph rooted in a given node.
     * This method performs a depth first post order traversal.
     * If ignoreInnerIris is active, an IRI node's hash depends on that of its children.
     * Otherewise, the IRI itself will be hashed.
     *
     * The method does not traverse over literals (as they do not have properties).
     *
     * @param rdfNode
     * @param useInnerIris
     * @param filterKeep (node, depth) whether to consider the node for id computation
     * @param seen
     * @param hashFn
     * @param priorHash
     * @return
     */
    public static HashCode createGenericHash(
            RDFNode rdfNode,
            boolean useInnerIris,
            BiPredicate<? super RDFNode, ? super Integer> filterKeep,
            int depth,
            Set<RDFNode> seen,
            HashFunction hashFn,
            Map<RDFNode, HashCode> priorHash) {

        Objects.requireNonNull(rdfNode);

        boolean consider = filterKeep.test(rdfNode, depth);

        HashCode result = priorHash.get(rdfNode);
        // It is valid to visit a node multiple times provided there exists a hash for it
        if(consider && result == null) {

            if(seen.contains(rdfNode)) {
//                if(rdfNode.isLiteral() || (rdfNode.isURIResource() && useInnerIris)) {
//                    return result;
//                } else {
                    throw new RuntimeException("Cannot hash graph with cycles - visited this node twice: " + rdfNode);
//                }
            }
            seen.add(rdfNode);

            boolean useIriOfThisNode = useInnerIris && rdfNode.isURIResource();
            if(rdfNode.isResource()) {

                Resource r = rdfNode.asResource();
                List<Statement> list = r.listProperties().toList();

                List<Entry<String, HashCode>> hashArgs = null;

                if(useIriOfThisNode) {
                    Node n = r.isAnon() ? Vars.x : r.asNode();
                    result = hashFn.hashString(NodeFmtLib.str(n), StandardCharsets.UTF_8);//Objects.toString(rdfNode);
                    priorHash.put(rdfNode, result);
                }

                // Always descend into nodes even if we going to use their id
                // The filterKeep controls whether to descend
                hashArgs = new ArrayList<>(list.size());
                for(Statement stmt : list) {
                    RDFNode child = stmt.getObject();
                    HashCode ch = createGenericHash(child, useInnerIris, filterKeep, depth + 1, seen, hashFn, priorHash);

                    if(ch != null) {
                        hashArgs.add(Maps.immutableEntry(stmt.getPredicate().getURI(), ch));
                    }
                }

                boolean isEffectiveLeaf = hashArgs.isEmpty();
                if(isEffectiveLeaf) {
                    if(r.isAnon()) {
                        // Using ntriples format because then the node id matches
                        RDFDataMgr.write(System.err, r.getModel(), RDFFormat.NTRIPLES);
    //                        RDFDataMgr.write(System.err, r.getModel(), RDFFormat.TURTLE_PRETTY);
                        throw new RuntimeException("Leaf nodes must not be blank nodes: " + NodeFmtLib.str(r.asNode()));
                    } else {
                        Node n = r.isAnon() ? Vars.x : r.asNode();
                        result = hashFn.hashString(NodeFmtLib.str(n), StandardCharsets.UTF_8);//Objects.toString(rdfNode);
                    }
                } else {

                    List<HashCode> hashCodes = hashArgs.stream()
                            .map(e -> hashFn.newHasher()
                                    .putString(e.getKey(), StandardCharsets.UTF_8)
                                    .putBytes(e.getValue().asBytes())
                                    .hash())
                            .collect(Collectors.toList());

                    result = Hashing.combineUnordered(hashCodes);
                    //ComparisonChain.start().compare(null, null).result();
                    //Entry x;
                    // Sort statements by (p, o)
//                    Collections.sort(hashArgs, (a, b) -> {
//                        int cmp = ComparisonChain.start()
//                            .compare(a.getKey(), b.getKey())
//                            .compare(a.getValue(), b.getValue())
//                            .result();
//                        return cmp;
//                    });

//                    result = HashUtils.computeHash(hashArgs);
                    //DcatUtils.getFirstDownloadUrl(dcatDataset)
                }
            } else {
                result = hashFn.hashString(NodeFmtLib.str(rdfNode.asNode()), StandardCharsets.UTF_8);
            }

            priorHash.put(rdfNode, result);
        }

        return result;
    }

}