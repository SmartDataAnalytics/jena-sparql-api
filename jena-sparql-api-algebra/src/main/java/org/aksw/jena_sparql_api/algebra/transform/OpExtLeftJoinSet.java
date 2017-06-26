package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/** N-way disjunction.
 *  OpUnion remains as the strict SPARQL algebra operator.
 */
public class OpExtLeftJoinSet extends OpExtN
{
    public static OpExtLeftJoinSet create() { return new OpExtLeftJoinSet() ; }

    public static Op create(Op left, Op right)
    {
        // Avoid stages of nothing
        if ( left == null && right == null )
            return null ;
        // Avoid stages of one.
        if ( left == null )
            return right ;
        if ( right == null )
            return left ;

        if ( left instanceof OpExtLeftJoinSet )
        {
        	OpExtLeftJoinSet opExtLeftJoinSet = (OpExtLeftJoinSet)left ;
        	opExtLeftJoinSet.add(right) ;
            return opExtLeftJoinSet ;
        }

//        if ( right instanceof OpDisjunction )
//        {
//            OpDisjunction opSequence = (OpDisjunction)right ;
//            // Add front.
//            opDisjunction.getElements().add(0, left) ;
//            return opDisjunction ;
//        }

        OpExtLeftJoinSet stage = new OpExtLeftJoinSet() ;
        stage.add(left) ;
        stage.add(right) ;
        return stage ;
    }

    private OpExtLeftJoinSet(List<Op> elts) { super("leftJoinSet", elts) ; }
    private OpExtLeftJoinSet() { super("leftJoinSet") ; }



    @Override
    public boolean equalTo(Op op, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op instanceof OpExtLeftJoinSet) ) return false ;
        OpExtLeftJoinSet other = (OpExtLeftJoinSet) op ;
        return super.equalsSubOps(other, labelMap) ;
    }

    @Override
    public Op apply(Transform transform, List<Op> elts)
    {
    	List<Op> args = new ArrayList<>(elts.size());
    	for(Op elt : elts) {
    		args.add(Transformer.transform(transform, elt));
    	}

    	OpExtLeftJoinSet result = new OpExtLeftJoinSet(args);

    	return result;
    }

    @Override
    public OpExtLeftJoinSet copy(List<Op> elts)
    {
        return new OpExtLeftJoinSet(elts) ;
    }

	@Override
	public Op effectiveOp() {
		OpSequence result = OpSequence.create();
		for(Op elt : elements) {
			result.add(elt);
		}
		return result;
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		// TODO Auto-generated method stub
		return null;
	}
}