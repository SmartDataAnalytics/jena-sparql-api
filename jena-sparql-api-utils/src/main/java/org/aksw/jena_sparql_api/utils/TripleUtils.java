package org.aksw.jena_sparql_api.utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.writer.NTriplesWriter;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

public class TripleUtils {

	public static Stream<Node> streamNodes(Triple t) {
		return Stream.of(t.getSubject(), t.getPredicate(), t.getObject());
	}

//    public static Multimap<Node, Triple> indexBySubject(Iterable<Triple> triples) {
//        Multimap<Node, Triple> result = indexBySubject(triples.iterator());
//        return result;
//    }
//
//    public static Multimap<Node, Triple> indexBySubject(Iterator<Triple> it) {
//        Multimap<Node, Triple> result = HashMultimap.create();
//        while(it.hasNext()) {
//            Triple triple = it.next();
//            Node s = triple.getSubject();
//
//            result.put(s, triple);
//        }
//
//        return result;
//    }

	public static Triple create(Node s, Node p, Node o, boolean swapSO) {
		Triple result = swapSO
			? new Triple(o, p, s)
			: new Triple(s, p, o);
			
		return result;
	}

    public static Node[] toArray(Triple t) {
        Node[] result = new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
        return result;
    }

    public static Triple fromArray(Node[] nodes) {
        Node s = nodes[0];
        Node p = nodes[1];
        Node o = nodes[2];
        Triple result = new Triple(s, p, o);
        return result;
    }

    public static Binding tripleToBinding(Triple triple) {
        BindingHashMap result = new BindingHashMap();

        tripleToBinding(triple, result);

        return result;
    }

    public static Binding tripleToBinding(Triple triple, BindingHashMap result) {
        result.add(Vars.s, triple.getSubject());
        result.add(Vars.p, triple.getPredicate());
        result.add(Vars.o, triple.getObject());

        return result;
    }


    public static String toNTripleString(Triple triple) {
        String s = NodeUtils.toNTriplesString(triple.getSubject());
        String p = NodeUtils.toNTriplesString(triple.getPredicate());
        String o = NodeUtils.toNTriplesString(triple.getObject());

        String result = s + " " + p + " " + o + " .";

        return result;
    }

    public static Triple swap(Triple t) {
        Triple result = new Triple(t.getObject(), t.getPredicate(), t.getSubject());
        return result;
    }

    public static Set<Triple> swap(Iterable<Triple> triples) {
        Set<Triple> result = new HashSet<Triple>();

        for(Triple t : triples) {
            result.add(swap(t));
        }

        return result;
    }

    public static String md5sum(Triple triple) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NTriplesWriter.write(baos, Collections.singleton(triple).iterator());
        String raw = baos.toString();
        String result = StringUtils.md5Hash(raw);

        return result;
    }

	public static Triple listToTriple(List<Node> nodes) {
	    return new Triple(nodes.get(0), nodes.get(1), nodes.get(2));
	}

	public static List<Node> tripleToList(Triple triple)
	{
	    List<Node> result = new ArrayList<Node>();
	    result.add(triple.getSubject());
	    result.add(triple.getPredicate());
	    result.add(triple.getObject());
	
	    return result;
	}
}