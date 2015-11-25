package org.aksw.jena_sparql_api.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NavigableMap;
import java.util.TreeMap;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.RDF;

public class RdfSeqUtils {
    public static final PropertyRelation seqRelation = PropertyRelation.create("?s ?p ?o . Filter(regex(?p, 'http://www.w3.org/1999/02/22-rdf-syntax-ns#_[0-9]+'))", "s", "p", "o");
    public static final Expr seqExpr = ExprUtils.parse("regex(?p, 'http://www.w3.org/1999/02/22-rdf-syntax-ns#_[0-9]+')");

    private static final Logger logger = LoggerFactory.getLogger(RdfSeqUtils.class);

    /**
     * Create a list from a navigable set with numbers as keys
     *
     * @param map
     * @return
     */
    public static <K extends Number & Comparable<? super K> , V> List<V> toList(Map<K, V> map) {
        Number tmp = Collections.max(map.keySet());
        int size = tmp == null ? 0 : tmp.intValue();

        List<V> result = new ArrayList<V>(size);
        result.addAll(Collections.nCopies(size, (V)null));

        for(Entry<? extends Number, V> entry : map.entrySet()) {
            Number key = entry.getKey();
            int index = key.intValue();

            V val = entry.getValue();
            result.set(index, val);
        }

        return result;
    }

    /**
     * Given a datasetGraph and an URI, obtain the raw list of nodes
     *
     * @param datasetGraph
     * @return
     */
    public List<Node> readSeq(DatasetGraph datasetGraph, Node g, Node s) {
        //QueryExecutionFactory qef = new QueryExecutionFactoryDatasetGraph(datasetGraph, true);
        //qef.createQueryExecution("Select ?p ?o {"
        Iterator<Quad> it = datasetGraph.find(g, s, Node.ANY, Node.ANY);
        NavigableMap<Integer, Node> indexToValue = new TreeMap<Integer, Node>();

        String prefix = RDF.getURI() + "_";
        int l = prefix.length();

        while(it.hasNext()) {
            Quad q = it.next();
            Node o = q.getObject();
            String p = q.getPredicate().getURI();

            boolean isMembership = p.startsWith(prefix);
            if(isMembership) {
                String sub = p.substring(l);
                Integer i = Integer.parseInt(sub);
                if(i != null) {
                    indexToValue.put(i - 1, o);
                } else {
                    logger.warn("Ignoring invalid index in seq: " + i);
                }
            }
        }

        List<Node> result = toList(indexToValue);
        return result;
    }


    public static void writeSeq(DatasetGraph target, List<Node> items, Node g, Node s) {

        // Write the ":s a rdf:Seq" quad
        Quad type = new Quad(g, s, RDF.type.asNode(), RDF.Seq.asNode());
        target.add(type);

        for(int i = 0; i < items.size(); ++i) {
            Node p = RDF.li(i + 1).asNode();
            Node o = items.get(i);
            if(o != null) {
                Quad quad = new Quad(g, s, p, o);
                target.add(quad);
            }
        }
    }

}
