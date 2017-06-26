package org.aksw.jena_sparql_api.jgrapht.transform;

import java.io.ByteArrayOutputStream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Graph which supports remapping nodes based on node transforms
 *
 * @author raven
 *
 */
public class GraphNodeRemapBase
    extends GraphBase
{
    protected Graph graph;
    protected NodeTransform fromGraph;// = new NodeTransformRenameMap(nodeToVar);
    protected NodeTransform toGraph;

    public GraphNodeRemapBase(Graph graph) {//, NodeTransform fromGraph, NodeTransform toGraph) {
        this.graph = graph;
        //super(graph);
//        this.fromGraph = fromGraph;
//        this.toGraph = toGraph;
    }



    //@Override
    public Graph getWrapped() {
        return graph;
    }


    @Override
    public void performAdd(Triple t) {
        Triple u = NodeTransformLib.transform(toGraph, t);
        graph.add(u);
    }


    @Override
    public void performDelete(Triple t) {
        Triple u = NodeTransformLib.transform(toGraph, t);
        graph.delete(u);
    }

    public static Triple createTriple(Node s, Node p, Node o) {
        Triple result = new Triple(
                s == null ? Node.ANY : s,
                p == null ? Node.ANY : p,
                o == null ? Node.ANY : o);
        return result;
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, this, RDFFormat.NTRIPLES);
        return out.toString();
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        Triple u = NodeTransformLib.transform(toGraph, m);
        return graph.find(u).mapWith(v -> NodeTransformLib.transform(fromGraph, v));
    }


//    @Override
//    public boolean contains(Triple t) {
//        Triple u = NodeTransformLib.transform(toGraph, t);
//        return graph.contains(u);
//    }


//    @Override
//    public ExtendedIterator<Triple> find(Triple m) {
//        Triple u = NodeTransformLib.transform(toGraph, m);
//        return graph.find(u).mapWith(v -> NodeTransformLib.transform(fromGraph, v));
//    }

//    @Override
//    public boolean contains(Node s, Node p, Node o) {
//        boolean result = contains(createTriple(s, p, o));
//        return result;
//    }

//    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
//        ExtendedIterator<Triple> result = find(createTriple(s, p, o));
//        return result;
//    }


//  public static Triple fromGraph(Triple t, Map<Node, Var> nodeToVar) {
//      Triple result = NodeTransformLib.transform(, t);
//      return result;
//  }
//
//  public static Triple toGraph(Triple t, Map<Var, Node> varToNode) {
//      Triple result = NodeTransformLib.transform(new NodeTransformRenameMap(varToNode), t);
//      return result;
//  }

//  @Override
//  public void remove(Node s, Node p, Node o) {
//      delete(createTriple(s, p, o));
//  }

}

