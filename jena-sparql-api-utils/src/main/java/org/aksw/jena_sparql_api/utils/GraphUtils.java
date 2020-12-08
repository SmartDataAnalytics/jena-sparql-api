package org.aksw.jena_sparql_api.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.graph.GraphWrapperTransform;
import org.aksw.jena_sparql_api.utils.io.NTripleUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.Streams;

public class GraphUtils {


    public static Stream<Node> streamNodes(Graph graph) {
        return stream(graph)
            .flatMap(TripleUtils::streamNodes);
    }

    /**
     * Remove all unused prefixes form the given graph's prefix mapping.
     * Scans all triples in the graph.
     *
     * @param graph The graph whose prefix mapping to optimize
     * @return The given graph
     */
    public static Graph optimizePrefixes(Graph graph) {
        PrefixMapping pm = graph.getPrefixMapping();
        PrefixMapping usedPrefixes = new PrefixMappingImpl();
        try(Stream<Node> nodeStream = streamNodes(graph)) {
            PrefixUtils.usedPrefixes(pm, nodeStream, usedPrefixes);
        }
        pm.clearNsPrefixMap();
        pm.setNsPrefixes(usedPrefixes);

        return graph;
    }

    public static Stream<Triple> stream(Graph graph) {
        ExtendedIterator<Triple> it = graph.find();
        return Streams.stream(it).onClose(it::close);
    }

    /**
     * Small convenience wrapper for default-loading of an RDF resource,
     * which would actually fit for RDFDataMgr
     *
     * FIXME Consolidate with RDFDataMgr{Ex, Rx}
     *
     * @param resourceName
     * @return
     */
    public static Stream<Triple> createTripleStream(String resourceName) {
        TypedInputStream in = RDFDataMgr.open(resourceName);
        Lang lang = RDFDataMgr.determineLang(resourceName, in.getContentType(), Lang.TURTLE);
        Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, lang, "http://example.org/");

        return Streams.stream(it);
    }

    public static Graph toMemGraph(Iterable<Triple> triples) {
        Graph result = GraphFactory.createGraphMem();
        triples.forEach(result::add);
        return result;
    }

    public static Map<Node, Graph> indexBySubject(Graph graph) {
        ExtendedIterator<Triple> it = graph.find(Node.ANY, Node.ANY, Node.ANY);
        Map<Node, Graph> result;
        try {
            result = indexBySubject(it);
        } finally {
            it.close();
        }
        return result;
    }

    public static Map<Node, Graph> indexBySubject(Iterable<Triple> triples) {
        Map<Node, Graph> result = indexBySubject(triples.iterator());
        return result;
    }

    public static Map<Node, Graph> indexBySubject(Iterator<Triple> it) {
        Map<Node, Graph> result = new HashMap<Node, Graph>();
        while(it.hasNext()) {
            Triple triple = it.next();
            Node s = triple.getSubject();

            Graph graph = result.get(s);
            if(graph == null) {
                graph = GraphFactory.createGraphMem();
                result.put(s,  graph);
            }
            graph.add(triple);
        }

        return result;
    }

    public static boolean isValid(Triple t) {
        boolean result;
        try {
            String str = NodeFmtLib.str(t) + " .";
            NTripleUtils.parseNTriplesString(str);
            result = true;
        } catch(Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * Fix for an issue we observed in some HDT files:
     * This method fixes triples that have a graph component in the object position by discarding
     * the graph from that object.
     *
     * The introduced overhead is a factor of a bit more than 3;
     * Tested on iterating the 1 million triples of a corrupted HDT file:
     *
     * Plain HDT: 1777ms ~ 1.5sec
     * Fixed HDT: 4922ms ~ 5sec
     *
     * @param t
     * @return
     */
    public static Triple fixTripleWithGraphInObject(Triple t) {

        // Only fix the object, therefore use short strings in s and p position
        // to speed up re-parsing
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<x:s> <x:p> ");
            sb.append(NodeFmtLib.str(t.getObject()));
            sb.append(" .");
            String str = sb.toString();
            Quad q = NTripleUtils.parseNQuadsString(str);
            Triple r = new Triple(t.getSubject(), t.getPredicate(), q.getObject());
            return r;
        } catch(Exception e) {
            return null;
        }
    }

//	public static Graph wrapWithNtripleParse(Graph base) {
//		return new GraphWrapperTransform(base, it -> it.filterKeep(GraphUtils::isValid));
//	}

    /**
     * We encountered HDT files that contained quads although the Java API
     * treated them as triples.
     * This wrapper causes all triples to be serialized as quads and then subsequently re-parsed.
     * Only the triple component is then returned.
     *
     *
     * @param base
     * @return
     */
    public static Graph wrapGraphWithNQuadsFix(Graph base) {
        return new GraphWrapperTransform(base, it -> it
                .mapWith(GraphUtils::fixTripleWithGraphInObject)
                .filterKeep(Objects::nonNull));
    }

    public static Graph wrapWithValidation(Graph base) {
        return new GraphWrapperTransform(base, it -> it
                .filterKeep(GraphUtils::isValid));
    }
}
