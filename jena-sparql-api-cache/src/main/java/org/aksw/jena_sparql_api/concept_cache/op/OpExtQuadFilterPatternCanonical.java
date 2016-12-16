package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.List;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.apache.jena.atlas.io.IndentedWriter;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpExtQuadFilterPatternCanonical
    extends OpExt
    implements OpCopyable
{
	private static final Logger logger = LoggerFactory.getLogger(OpExtQuadFilterPatternCanonical.class);

    public OpExtQuadFilterPatternCanonical(QuadFilterPatternCanonical qfpc) {
        super(OpExtQuadFilterPatternCanonical.class.getSimpleName());

        this.qfpc = qfpc;
    }

    private QuadFilterPatternCanonical qfpc;

    public QuadFilterPatternCanonical getQfpc() {
        return qfpc;
    }

    @Override
    public Op effectiveOp() {

    	//System.out.println("Not sure if it is a good idea for " + OpExtQuadFilterPatternCanonical.class.getName() + " getting called");
    	Op result = qfpc.toOp();
//    	if(result instanceof OpFilter) {
//    		// HACK: skip the filter, otherwise OpVars.visibleVars() won't work with Jena 3.1.0
//        	logger.warn(OpExtQuadFilterPatternCanonical.class.getName() + ".effectiveOp() hack used");
//    		result = ((OpFilter)result).getSubOp();
//    	}

        // TODO Auto-generated method stub
        return result;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        out.println("" + qfpc);
        // TODO Auto-generated method stub

    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public Op copy(List<Op> subOps) {
		OpExtQuadFilterPatternCanonical result = new OpExtQuadFilterPatternCanonical(qfpc);
		return result;
	}

}