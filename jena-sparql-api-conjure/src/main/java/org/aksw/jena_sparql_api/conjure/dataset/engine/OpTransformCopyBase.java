package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpError;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpMacroCall;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpQueryOverViews;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpStmtList;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen;
import org.apache.jena.sparql.algebra.Op;

//public class OpTransformCopyBase
//	implements OpTransform
//{
//
//	@Override
//	public Op visit(OpData op) {
//		return null;
//	}
//
//	@Override
//	public Op visit(OpDataRefResource op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpConstruct op, Op subOp) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpUpdateRequest op, Op subOp) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpStmtList op, Op subOp) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpUnion op, List<Op> subOps) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpCoalesce op, Op subOp) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpPersist op, Op subOp) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpHdtHeader op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpMacroCall op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpSequence op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpSet op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpWhen op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpError op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpVar op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Op visit(OpQueryOverViews op) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
