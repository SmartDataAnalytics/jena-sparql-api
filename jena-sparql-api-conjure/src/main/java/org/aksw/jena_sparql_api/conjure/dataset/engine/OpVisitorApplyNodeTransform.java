package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpQueryOverViews;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpStmtList;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.sparql.graph.NodeTransform;


public class OpVisitorApplyNodeTransform
	extends OpVisitorAdapter<Op>
{
	protected NodeTransform nodeTransform;
	protected SparqlStmtParser parser;
	
	public OpVisitorApplyNodeTransform(NodeTransform nodeTransform, SparqlStmtParser parser) {
		super();
		this.nodeTransform = nodeTransform;
		this.parser = parser;
	}

	public <T extends Collection<String>> T applyNodeTransform(Collection<String> strs, T output) {
		for(String str : strs) {
			String after = applyNodeTransform(str);
			output.add(after);
		}
		
		return output;
	}

	public String applyNodeTransform(String str) {
		SparqlStmt before = parser.apply(str);
		SparqlStmt after = SparqlStmtUtils.applyNodeTransform(before, nodeTransform);
		return after.toString();
	}
	
	@Override
	public Op visit(OpConstruct op) {
		Collection<String> strs = applyNodeTransform(op.getQueryStrings(), new ArrayList<>());
		op.setQueryStrings(strs);		
		return op;
	}
	
	@Override
	public Op visit(OpData op) {
		return null;
	}


	@Override
	public Op visit(OpUpdateRequest op) {
		Collection<String> strs = applyNodeTransform(op.getUpdateRequests(), new ArrayList<>());
		op.setUpdateRequests(strs);		
		return op;
	}
	
	@Override
	public Op visit(OpStmtList op) {
		List<String> strs = applyNodeTransform(op.getStmts(), new ArrayList<>());
		op.setStmts(strs);
		return op;
	}
	
	@Override
	public Op visit(OpQueryOverViews op) {
		List<String> strs = applyNodeTransform(op.getViewDefs(), new ArrayList<>());
		op.setViewDefs(strs);
		return op;		
	}
}
