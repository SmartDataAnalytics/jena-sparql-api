package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
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

// Maybe we can get away with a more light-weight approach than to add all this transformer/visitor
// infrastructure, using an OpUtils.transform(op, visitor) method
// 
public interface OpTransform {
	Op visit(OpData op);
	Op visit(OpDataRefResource op);
	Op visit(OpConstruct op, Op subOp);
	Op visit(OpUpdateRequest op, Op subOp);
	Op visit(OpStmtList op, Op subOp);
	Op visit(OpUnion op, List<Op> subOps);
	Op visit(OpCoalesce op, Op subOp);
	Op visit(OpPersist op, Op subOp);

	Op visit(OpHdtHeader op);


	Op visit(OpMacroCall op);
	Op visit(OpSequence op);
	Op visit(OpSet op);
	Op visit(OpWhen op);
	Op visit(OpError op);


	Op visit(OpVar op);
	
	Op visit(OpQueryOverViews op);
}
