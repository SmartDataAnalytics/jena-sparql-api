package org;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;

public class TransformJoinToConjunction
    extends TransformCopy 
{
    public static final TransformJoinToConjunction fn = new TransformJoinToConjunction();
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        //OpDisjunction result = OpDisjunction.create();
        OpSequence result = OpSequence.create();
        
        add(result, left);
        add(result, right);

        return result;        
    }
    
    public static void add(OpSequence dest, Op op) {
        if(op instanceof OpSequence) {
            OpSequence o = (OpSequence)op;
            for(Op subOp : o.getElements()) {
                dest.add(subOp);
            }
        } else if(op instanceof OpJoin) {
            OpUnion o = (OpUnion)op;
            dest.add(o.getLeft());
            dest.add(o.getRight());
        } else {
            dest.add(op);
        }
    }
}
