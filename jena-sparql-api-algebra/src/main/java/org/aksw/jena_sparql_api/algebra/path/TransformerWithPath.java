package org.aksw.jena_sparql_api.algebra.path;

import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;

public class TransformerWithPath {
    public static Op transform(PathState pathState, Function<? super PathState, ? extends TransformCopyWithPath> transformCtor) {
        TransformCopyWithPath transform = transformCtor.apply(pathState);
        OpVisitor beforeVisitor = transform.getBeforeVisitor();
        return transform(pathState, transform, new ExprTransformCopy(), beforeVisitor);
    }

    public static Op transform(PathState pathState, Transform opTransform, ExprTransform exprTransform, OpVisitor beforeVisitor) {
        ApplyTransformVisitorWithPath applyTransformVisitor = new ApplyTransformVisitorWithPath(pathState, opTransform, exprTransform, false, beforeVisitor, null);


        WalkerVisitorWithPath walker = new WalkerVisitorWithPath(pathState, applyTransformVisitor, applyTransformVisitor, beforeVisitor, null);
        Op inputOp = pathState.getPathToOp().get(pathState.getPath());
        walker.walk(inputOp);

        Op result = applyTransformVisitor.opResult();

        return result;
    }
}
