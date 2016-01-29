package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

public class QuadUtils {


    public static Map<Node, Set<Quad>> partitionByGraph(Iterable<Quad> quads) {
        Map<Node, Set<Quad>> result = new HashMap<Node, Set<Quad>>();
        for (Quad quad : quads) {
            Node g = quad.getGraph();
            Set<Quad> qs = result.get(g);
            if (qs == null) {
                qs = new HashSet<Quad>();
                result.put(g, qs);
            }
            qs.add(quad);
        }
        return result;
    }

    public static Map<Node, Set<Triple>> partitionByGraphTriples(Iterable<Quad> quads) {
        Map<Node, Set<Triple>> result = new HashMap<Node, Set<Triple>>();
        for (Quad quad : quads) {
            Node g = quad.getGraph();
            Set<Triple> ts = result.get(g);
            if (ts == null) {
                ts = new HashSet<Triple>();
                result.put(g, ts);
            }
            Triple t = quad.asTriple();
            ts.add(t);
        }
        return result;
    }


    public static Element toElement(Iterable<Quad> quads) {
        Map<Node, Set<Quad>> map = partitionByGraph(quads);
        Element result = toElement(map);
        return result;
    }

    public static Element toElement(Map<Node, Set<Quad>> graphToQuads) {
        ElementGroup es = new ElementGroup();
        for(Entry<Node, Set<Quad>> entry : graphToQuads.entrySet()) {
            ElementTriplesBlock e = new ElementTriplesBlock();
            for(Quad quad : entry.getValue()) {
                Triple triple = quad.asTriple();
                e.addTriple(triple);
            }

            Node graph = entry.getKey();

            Element f = graph == null || graph.equals(Quad.defaultGraphNodeGenerated)
                    ? e
                    : new ElementNamedGraph(graph, e);

            es.addElement(f);
        }

        Element result = es.getElements().size() == 1
                ? es.getElements().get(0)
                : es;

        return result;
    }


    public static final String ng = "g";
    public static final String ns = "s";
    public static final String np = "p";
    public static final String no = "o";
    public static final List<String> quadVarNames = Arrays.asList(ng, ns, np,
            no);

    public static final Var vg = Var.alloc(ng);
    public static final Var vs = Var.alloc(ns);
    public static final Var vp = Var.alloc(np);
    public static final Var vo = Var.alloc(no);

    public static final List<Var> quadVars = Arrays.asList(vg, vs, vp, vo);

    public static Binding quadToBinding(Quad quad) {
        BindingHashMap result = new BindingHashMap();

        result.add(vg, quad.getGraph());
        result.add(vs, quad.getSubject());
        result.add(vp, quad.getPredicate());
        result.add(vo, quad.getObject());

        return result;
    }

    /**
     * Substitutes the keys in the map
     *
     * @param <K>
     * @param <V>
     * @param original
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> copySubstitute(Map<K, V> original,
            Map<K, K> map) {
        Map<K, V> result = new HashMap<K, V>();
        for (Map.Entry<K, V> entry : original.entrySet()) {
            result.put(MapUtils.getOrElse(map, entry.getKey(), entry.getKey()),
                    entry.getValue());
        }

        return result;
    }

    public static Set<Quad> applyNodeTransform(Set<Quad> quads,
            NodeTransform nodeTransform) {
        Set<Quad> result = new HashSet<Quad>();
        for (Quad quad : quads) {
            Quad newQuad = applyNodeTransform(quad, nodeTransform);
            result.add(newQuad);
        }

        return result;
    }

    public static Quad applyNodeTransform(Quad quad,
            NodeTransform nodeTransform) {
        Node g = nodeTransform.apply(quad.getGraph());
        Node s = nodeTransform.apply(quad.getSubject());
        Node p = nodeTransform.apply(quad.getPredicate());
        Node o = nodeTransform.apply(quad.getObject());

        g = g != null ? g : quad.getGraph();
        s = s != null ? s : quad.getSubject();
        p = p != null ? p : quad.getPredicate();
        o = o != null ? o : quad.getObject();

        Quad result = new Quad(g, s, p, o);
        return result;
    }

    public static Quad copySubstitute(Quad quad,
            Map<? extends Node, ? extends Node> map) {
        return new Quad(
                MapUtils.getOrElse(map, quad.getGraph(), quad.getGraph()),
                MapUtils.getOrElse(map, quad.getSubject(), quad.getSubject()),
                MapUtils.getOrElse(map, quad.getPredicate(),
                        quad.getPredicate()),
                MapUtils.getOrElse(map, quad.getObject(), quad.getObject()));
    }

    /**
     * Create a quad from an array
     *
     * @param nodes
     * @return
     */
    public static Quad create(Node[] nodes) {
        return new Quad(nodes[0], nodes[1], nodes[2], nodes[3]);
    }

    public static Node getNode(Quad quad, int index) {
        switch (index) {
        case 0:
            return quad.getGraph();
        case 1:
            return quad.getSubject();
        case 2:
            return quad.getPredicate();
        case 3:
            return quad.getObject();
        default:
            throw new IndexOutOfBoundsException(
                    "Index: " + index + " Size: " + 4);
        }
    }

    public static Node substitute(Node node, Binding binding) {
        Node result = node;

        if (node.isVariable()) {
            result = binding.get((Var) node);
            if (result == null) {
                throw new RuntimeException("Variable " + node + "not bound");
            }
        }

        return result;
    }

    public static Quad copySubstitute(Quad quad, Binding binding) {
        return new Quad(substitute(quad.getGraph(), binding),
                substitute(quad.getSubject(), binding),
                substitute(quad.getPredicate(), binding),
                substitute(quad.getObject(), binding));
    }

    /*
     * public static QuadPattern copySubstitute(QuadPattern quadPattern, Binding
     * map) { map.ge }
     */

    public static QuadPattern copySubstitute(QuadPattern quadPattern,
            Map<? extends Node, ? extends Node> map) {
        QuadPattern result = new QuadPattern();
        for (Quad quad : quadPattern) {
            result.add(copySubstitute(quad, map));
        }

        return result;
    }

    public static Quad listToQuad(List<Node> nodes) {
        return new Quad(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));
    }

    public static List<Node> quadToList(Quad quad) {
        List<Node> result = new ArrayList<Node>();
        result.add(quad.getGraph());
        result.add(quad.getSubject());
        result.add(quad.getPredicate());
        result.add(quad.getObject());

        return result;
    }

    public static Set<Var> getVarsMentioned(Quad quad) {
        return NodeUtils.getVarsMentioned(quadToList(quad));
    }

    public static Map<Node, Node> getVarMapping(Quad a, Quad b) {
        List<Node> nAs = quadToList(a);
        List<Node> nBs = quadToList(b);

        Map<Node, Node> result = new HashMap<Node, Node>();
        for (int i = 0; i < 4; ++i) {
            Node nA = nAs.get(i);
            Node nB = nBs.get(i);

            if (nA.isVariable()) {
                Map<Node, Node> newEntry = Collections.singletonMap(nA, nB);

                // MapUtils.isCompatible(result, newEntry);

                result.putAll(newEntry);
            } else {
                if (!nA.equals(nB)) {
                    return null;
                }
            }
        }

        return result;
    }
}
