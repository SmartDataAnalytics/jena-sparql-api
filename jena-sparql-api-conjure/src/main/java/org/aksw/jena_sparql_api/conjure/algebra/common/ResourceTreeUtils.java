package org.aksw.jena_sparql_api.conjure.algebra.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.conjure.entity.algebra.HashUtils;
import org.apache.jena.ext.com.google.common.collect.ComparisonChain;
import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.ext.com.google.common.hash.HashFunction;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;

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
				StringBuilder sb = new  StringBuilder();
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
	 * For each of a reosurce's statement, first a hash for its object recursively computed.
	 * Afterwards, the so obtained (property, hash) pairs are sorted (by property first) and compute a hash from
	 * that list.
	 * 
	 * Note, that URIs of nodes are NOT considered unless they are leaf nodes.
	 * 
	 * ISSUE We need to be able to 'skip' nodes 
	 * 
	 * @param rdfNode
	 * @return
	 */
	public static String createGenericHash(RDFNode rdfNode) {
		Set<RDFNode> seen = new HashSet<>();
		Map<RDFNode, String> priorHash = new HashMap<>();
		String result = createGenericHash(rdfNode, seen, priorHash);
		return result;
	}

	public static HashCode createGenericHash2(RDFNode rdfNode) {
		String str = createGenericHash(rdfNode);
		HashCode result = Hashing.sha256().hashString(str, StandardCharsets.UTF_8);
		return result;
	}


	public static String createGenericHash(RDFNode rdfNode, Set<RDFNode> seen, Map<RDFNode, String> priorHash) {
		
		String result = priorHash.get(rdfNode);
		if(result == null) {

			if(seen.contains(rdfNode)) {
				throw new RuntimeException("Cannot hash graph with cycles - visited this node twice: " + rdfNode);
			}
			seen.add(rdfNode);

			if(rdfNode.isResource()) {
				Resource r = rdfNode.asResource();
				List<Statement> list = r.listProperties().toList();
				
				if(list.isEmpty()) {
					result = Objects.toString(rdfNode);
				} else {
				
					List<Entry<String, String>> hashArgs = new ArrayList<>(list.size());
					for(Statement stmt : list) {
						RDFNode child = stmt.getObject();
						String ch = createGenericHash(child, seen, priorHash);
						hashArgs.add(Maps.immutableEntry(stmt.getPredicate().getURI(), ch));
					}
	
					//ComparisonChain.start().compare(null, null).result();
					//Entry x;
					// Sort statements by (p, o)
					Collections.sort(hashArgs, (a, b) -> {
						int cmp = ComparisonChain.start()
							.compare(a.getKey(), b.getKey())
							.compare(a.getValue(), b.getValue())
							.result();
						return cmp;
					});
	
					result = HashUtils.computeHash(hashArgs);
					//DcatUtils.getFirstDownloadUrl(dcatDataset)
				}
			} else {
				result = HashUtils.computeHash(rdfNode.toString());
			}
		}

		priorHash.put(rdfNode, result);
		return result;
	}

}