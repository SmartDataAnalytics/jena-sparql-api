package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.BiMap;


/**
 * Combines an RDF graph with meta information on how nodes map
 * to Ops and Exprs
 * 
 * 
 * @author raven Oct 13, 2017
 *
 */
public class OpGraph {
	// TODO Add an attribute for the OP nodes
	
	protected BiMap<Node, Expr> nodeToExpr;
	protected BiMap<Node, Quad> nodeToQuad;
	
	protected Graph jenaGraph;
	protected org.jgrapht.Graph<Node, Triple> jgraphTGraph;

	public OpGraph(Graph jenaGraph, BiMap<Node, Expr> nodeToExpr, BiMap<Node, Quad> nodeToQuad) {
		super();
		this.jenaGraph = jenaGraph;
		this.nodeToExpr = nodeToExpr;

		this.jgraphTGraph = new PseudoGraphJenaGraph(jenaGraph);
	}

	public BiMap<Node, Expr> getNodeToExpr() {
		return nodeToExpr;
	}

	public BiMap<Node, Quad> getNodeToQuad() {
		return nodeToQuad;
	}
	
	public Graph getJenaGraph() {
		return jenaGraph;
	}
	
	public org.jgrapht.Graph<Node, Triple> getJGraphTGraph() {
		return jgraphTGraph;
	}
	
	
}
