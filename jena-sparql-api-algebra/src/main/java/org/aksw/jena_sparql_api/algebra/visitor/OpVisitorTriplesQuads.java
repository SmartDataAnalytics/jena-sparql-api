package org.aksw.jena_sparql_api.algebra.visitor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Quad;

public class OpVisitorTriplesQuads
	extends OpVisitorBase
{
	protected List<Triple> triples;
	protected List<Quad> quads;

	
	public static List<Quad> extractQuadsSafe(Op op) {
		OpVisitorTriplesQuads visitor = new OpVisitorTriplesQuads();
		op.visit(visitor);
		List<Quad> result = visitor.forceGetQuadList();
		return result;
	}
	
	
	public static Stream<Quad> streamQuads(Op op) {
		return extractQuadsSafe(op).stream();
	}
	
	
	protected boolean isTriplesOrQuads() {
		return  quads != null || triples != null;
	}

	public List<Triple> getTriples() {
		return triples;
	}
	
	public List<Quad> getQuads() {
		return quads;
	}
	
	/**
	 * Always return a (possibly empty) list of quads even if the op did not have any quads.
	 * Triples are converted to quads with the graph set to {@link Quad#defaultGraphNodeGenerated}.
	 */
	public List<Quad> forceGetQuadList() {
		List<Quad> result = triples != null
				? triples.stream().map(t -> new Quad(Quad.defaultGraphNodeGenerated, t)).collect(Collectors.toList())
				: quads != null
					? quads
					: Collections.emptyList();
		return result;
	}
	
//	protected void initQuadsFromTriples() {
//		quads = triples.stream().map(t -> new Quad(Quad.defaultGraphNodeGenerated, t)).collect(Collectors.toList());
//	}
	
	@Override
	public void visit(OpTriple op) {
		triples = Collections.singletonList(op.getTriple());
	}
	
	@Override
	public void visit(OpBGP op) {
		triples = op.getPattern().getList();
	}
	
	@Override
	public void visit(OpQuad op) {
		quads = Collections.singletonList(op.getQuad());
	}

	@Override
	public void visit(OpQuadPattern op) {
		quads = op.getPattern().getList();
	}

	@Override
	public void visit(OpQuadBlock op) {
		quads = op.getPattern().getList();
		
	}
}
