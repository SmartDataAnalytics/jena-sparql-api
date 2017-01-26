package org.aksw.jena_sparql_api.concept_cache.op;

import java.util.List;

import org.aksw.jena_sparql_api.concept_cache.domain.ConjunctiveQuery;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpExtConjunctiveQuery
    extends OpExt
    implements OpCopyable
{
	private static final Logger logger = LoggerFactory.getLogger(OpExtConjunctiveQuery.class);

    private ConjunctiveQuery conjunctiveQuery;

    public OpExtConjunctiveQuery(ConjunctiveQuery conjunctiveQuery) {
        super(OpExtConjunctiveQuery.class.getSimpleName());

        this.conjunctiveQuery = conjunctiveQuery;
    }

    public ConjunctiveQuery getQfpc() {
        return conjunctiveQuery;
    }

    @Override
    public Op effectiveOp() {

    	//System.out.println("Not sure if it is a good idea for " + OpExtQuadFilterPatternCanonical.class.getName() + " getting called");
    	Op result = conjunctiveQuery.toOp();
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
        out.println("" + conjunctiveQuery);
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
		OpExtConjunctiveQuery result = new OpExtConjunctiveQuery(conjunctiveQuery);
		return result;
	}

}