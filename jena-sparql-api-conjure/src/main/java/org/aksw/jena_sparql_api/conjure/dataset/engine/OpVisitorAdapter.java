package org.aksw.jena_sparql_api.conjure.dataset.engine;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpError;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJobInstance;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpMacroCall;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpQueryOverViews;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpStmtList;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen;

public class OpVisitorAdapter<T>
	implements OpVisitor<T>
{
	@Override
	public T visit(OpData op) {
		return null;
	}

	@Override
	public T visit(OpDataRefResource op) {
		return null;
	}

	@Override
	public T visit(OpConstruct op) {
		return null;
	}

	@Override
	public T visit(OpUpdateRequest op) {
		return null;
	}

	@Override
	public T visit(OpUnion op) {
		return null;
	}

	@Override
	public T visit(OpCoalesce op) {
		return null;
	}

	@Override
	public T visit(OpPersist op) {
		return null;
	}

	@Override
	public T visit(OpHdtHeader op) {
		return null;
	}

	@Override
	public T visit(OpMacroCall op) {
		return null;
	}

	@Override
	public T visit(OpSequence op) {
		return null;
	}

	@Override
	public T visit(OpSet op) {
		return null;
	}

	@Override
	public T visit(OpWhen op) {
		return null;
	}

	@Override
	public T visit(OpError op) {
		return null;
	}

	@Override
	public T visit(OpVar op) {
		return null;
	}

	@Override
	public T visit(OpStmtList op) {
		return null;
	}

	@Override
	public T visit(OpQueryOverViews op) {
		return null;
	}
	
	@Override
	public T visit(OpJobInstance op) {
		return null;
	}
}
