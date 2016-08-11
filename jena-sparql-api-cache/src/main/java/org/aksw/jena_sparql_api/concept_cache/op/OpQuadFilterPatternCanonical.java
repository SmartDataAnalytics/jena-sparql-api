package org.aksw.jena_sparql_api.concept_cache.op;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.apache.jena.atlas.io.IndentedWriter;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpQuadFilterPatternCanonical
    extends OpExt
{
    public OpQuadFilterPatternCanonical(QuadFilterPatternCanonical qfpc) {
        super(OpQuadFilterPatternCanonical.class.getSimpleName());

        this.qfpc = qfpc;
    }

    private QuadFilterPatternCanonical qfpc;

    public QuadFilterPatternCanonical getQfpc() {
        return qfpc;
    }

    @Override
    public Op effectiveOp() {http://www.welt.de/
        // TODO Auto-generated method stub
        return null;
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

}