package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Var;

public class TransformRemoveGraph
    extends TransformCopy
{
//    protected Stack<ExprList> stack = new Stack<>();
    //protected Set<Var> userVars; /Varhese vari
    protected Predicate<Var> preventRemoval; // graphs making use of such variables are NOT removed

    public TransformRemoveGraph(Predicate<Var> preventRemoval) {
        super();
        this.preventRemoval = preventRemoval;
    }

//    @Override
//    public Op transform(OpFilter op, Op subOp) {
//        ExprList exprs = op.getExprs();
//        stack.push(exprs);
//
//        Op newSubOp = Transformer.transform(this, subOp);
//
//        Op result = newSubOp == subOp ?op : OpFilter.filter(exprs, newSubOp);
//        stack.pop();
//
//        return result;
//    }

    @Override
    public Op transform(OpQuadPattern opQuadPattern) {
        throw new RuntimeException("Transformation on quad form not supported");
    }

    @Override
    public Op transform(OpQuadBlock opQuadPattern) {
        throw new RuntimeException("Transformation on quad form not supported");
    }

    @Override
    public Op transform(OpGraph op, Op subOp) {
        // If the variable of the graph
        Node node = op.getNode();
        boolean remove = node.isVariable() && !preventRemoval.test((Var)node);

        Op result = remove
            ? subOp
            : new OpGraph(node, subOp)
            ;

        return result;
    }

//
//    public static void transform() {
//        ElementTransform elementTransform = ElementTransformDatasetDescription.create(graphs, element, dd);
//        ElementVisitor beforeVisitor = new ElementVisitorBase() {
//            @Override
//            public void visit(ElementNamedGraph el) {
//                graphs.push(el.getGraphNameNode());
//                //System.out.println("push " + el.getGraphNameNode());
//            }
//        };
//        ElementVisitor afterVisitor = new ElementVisitorBase() {
//            @Override
//            public void visit(ElementNamedGraph el) {
//                graphs.pop();
//                //System.out.println("pop " + el.getGraphNameNode());
//            }
//        };
//
//        Element result = ElementTransformer.transform(element, elementTransform, exprTransform, beforeVisitor, afterVisitor);
//
//    }

}
