package org.aksw.commons.graph.index.jena.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.analysis.DistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.utils.OpCopyable;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpDistinctExtendFilter
    extends OpExt
    implements OpCopyable
{
    protected DistinctExtendFilter def;
    protected Op subOp;

    public OpDistinctExtendFilter(Op subOp, Collection<Var> initialVars) {
        super(OpDistinctExtendFilter.class.getSimpleName());
        this.subOp = subOp;

        this.def = DistinctExtendFilter.create(initialVars);
    }

    public DistinctExtendFilter getDef() {
        return def;
    }

    public Op getSubOp() {
        return subOp;
    }

    @Override
    public Op effectiveOp() {
        Op result = def.toOp(subOp);

        return result;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        return null;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        Op tmp = effectiveOp();
        tmp.output(out);

//    	out.println("" + def);
//        out.incIndent();
//        subOp.output(out);
//        out.decIndent();
        //out.println();
    }

    @Override
    public boolean equalTo(Op obj, NodeIsomorphismMap labelMap) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        OpDistinctExtendFilter other = (OpDistinctExtendFilter) obj;
        Op a = effectiveOp();
        Op b = other.effectiveOp();
        boolean result = a.equalTo(b, labelMap);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * effectiveOp().hashCode();
        return result;
    }

    @Override
    public Op copy(List<Op> subOps) {
        if(subOps.size() != 1) {
            throw new RuntimeException("Exactly 1 subOp expected, instead got: " + subOps);
        }

        Op subOp = subOps.get(0);
        Set<Var> initialVars = OpVars.visibleVars(subOp);
        OpDistinctExtendFilter result = new OpDistinctExtendFilter(subOps.get(0), initialVars);
        return result;
    }

    @Override
    public List<Op> getElements() {
        return Collections.singletonList(subOp);
    }


//    @Override
//    public Op apply(Transform transform, List<Op> elts) {
//        throw new RuntimeException("not implemented yet");
//    }

}
