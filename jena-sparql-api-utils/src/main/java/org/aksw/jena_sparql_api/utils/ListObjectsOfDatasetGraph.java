package org.aksw.jena_sparql_api.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.utils.functions.F_QuadGetObject;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/**
 * TODO This list must register itself as a listener to the datasetGraph.
 *
 * @author raven
 *
 */
public class ListObjectsOfDatasetGraph
    extends AbstractList<Node>
{
    protected DatasetGraph datasetGraph;
    protected Quad quad;
    protected List<Node> objects;

    public ListObjectsOfDatasetGraph(DatasetGraph datasetGraph, Quad quad, List<Node> objects) {
        this.datasetGraph = datasetGraph;
        this.quad = quad;
        this.objects = objects;
    }

    public Quad createQuad(Node o) {
        Node g = quad.getGraph();
        Node s = quad.getSubject();
        Node p = quad.getPredicate();

        Quad result = new Quad(g, s, p, o);

        return result;
    }

    @Override
    public boolean add(Node o) {
        Quad quad = createQuad(o);

        boolean isContained = datasetGraph.contains(quad);
        if(!isContained) {
            datasetGraph.add(quad);
        }

        return true;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = objects.contains(o);
        return result;
    }

    @Override
    public boolean remove(Object obj) {
        boolean result = false;
        if(obj instanceof Node) {
            Node o = (Node)obj;

            objects.remove(o);

            Quad quad = createQuad(o);

            result = datasetGraph.contains(quad);
            datasetGraph.delete(quad);
        }

        return result;
    }

    public static List<Node> getObjects(DatasetGraph datasetGraph, Quad quad) {
        Iterator<Quad> it = datasetGraph.find(quad);
        List<Quad> quads = Lists.newArrayList(it);
        List<Node> result = Lists.transform(quads, F_QuadGetObject.fn);
        return result;
    }

    @Override
    public Node get(int index) {
        //List<Node> objects = getObjects(datasetGraph, quad);
        Node result = objects.get(index);
        return result;
    }

    @Override
    public Node remove(int index) {
        //List<Node> objects = getObjects(datasetGraph, quad);
        Node result = objects.get(index);

        Quad quad = createQuad(result);
        datasetGraph.delete(quad);
        return result;
    }

    /**
     * TODO We need to re-insert all quads if we use the setter
     *
     */
    @Override
    public Node set(int index, Node node) {
        //List<Node> objects = getObjects(datasetGraph, quad);
        Node old = objects.get(index);
        objects.set(index, node);

        Quad oq = createQuad(old);
        Quad nq = createQuad(node);

        datasetGraph.delete(oq);
        datasetGraph.add(nq);

        return node;
    }

    @Override
    public int size() {
        Iterator<Quad> it = datasetGraph.find(quad);
        int result = Iterators.size(it);
        return result;
    }

    public static ListObjectsOfDatasetGraph create(DatasetGraph datasetGraph, Quad quad) {
        List<Node> objects = new ArrayList<Node>(getObjects(datasetGraph, quad));

        ListObjectsOfDatasetGraph result = new ListObjectsOfDatasetGraph(datasetGraph, quad, objects);
        return result;
    }

    public static ListObjectsOfDatasetGraph create(DatasetGraph datasetGraph, Node g, Node s, Node p) {
        Quad quad = new Quad(g, s, p, Node.ANY);
        ListObjectsOfDatasetGraph result = create(datasetGraph, quad);
        return result;
    }
}

