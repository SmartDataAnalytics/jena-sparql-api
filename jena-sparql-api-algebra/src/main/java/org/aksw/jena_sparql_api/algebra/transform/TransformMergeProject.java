package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpProject;

/**
 * project(project(op, varsA), varsB) -> project(op, VarsB)
 *
 * Assumes that the op expression is valid, such that only the variables of the outer most projection need to be retained.
 *
 * @author raven
 *
 */
public class TransformMergeProject
    extends TransformCopy
{

    public static Op transform(Op op) {
        Transform transform = new TransformMergeProject();
        Op result = Transformer.transform(transform, op);
        return result;
    }


    @Override
    public Op transform(OpProject op, Op subOp) {
        Op result = subOp instanceof OpProject
            ? new OpProject(((OpProject)subOp).getSubOp(), op.getVars())
            : new OpProject(subOp, op.getVars())
            ;

        return result;

//    	List<Var> vars;
//        if(subOp instanceof OpProject) {
//            OpProject x = (OpProject)subOp;
//            Set<Var> vs = new LinkedHashSet<>(op.getVars());
//            vs.addAll(x.getVars());
//
//            vars = new ArrayList<Var>(vs);
//        } else {
//        	result = new OpProj
//        }
//            ? new
//            : null;
//
//        new OpProject()
//
//        return super.transform(opProject, subOp);
    }

}
