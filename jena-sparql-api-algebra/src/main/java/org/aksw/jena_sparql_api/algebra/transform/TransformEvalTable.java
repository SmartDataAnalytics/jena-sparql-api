package org.aksw.jena_sparql_api.algebra.transform;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/**
 * A transformer that evaluates every op whose sub ops are all {@link OpTable} instances
 * to another {@link OpTable} instance.
 * Note, that this mechanism materializes every intermediate result as a table.
 *
 * A more efficient approach may be to evaluate sub-expressions of an op using an {@link OpExecutor} and
 * substituting the roots of these sub-expressions in op with the obtained results.
 *
 * @author raven
 *
 */
public class TransformEvalTable
    extends TransformCopyAdapter
{
    protected OpExecutor opExecutor;
    protected ExecutionContext execCxt;

    public TransformEvalTable(OpExecutor opExecutor, ExecutionContext execCxt) {
        super();
        this.opExecutor = opExecutor;
        this.execCxt = execCxt;
    }

    public static TransformEvalTable create() {
        OpExecutorFactory opExecutorFactory = OpExecutor.stdFactory;
        ExecutionContext execCxt = createExecCxt(opExecutorFactory);
        OpExecutor opExecutor = opExecutorFactory.create(execCxt);

        return new TransformEvalTable(opExecutor, execCxt);
    }

    //protected QueryIterRoot
    public static ExecutionContext createExecCxt(OpExecutorFactory opExecutorFactory) {
        Context cxt = ARQ.getContext().copy() ;
        cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        DatasetGraph dataset = DatasetGraphFactory.create();
        ExecutionContext execCxt = new ExecutionContext(cxt, dataset.getDefaultGraph(), dataset, opExecutorFactory);

        return execCxt;
    }

    public OpTable exec(Op op) {
        QueryIterator queryIter = opExecutor.executeOp(op , QueryIterRoot.create(execCxt));
        Table table = new TableN(queryIter);
        return OpTable.create(table);
    }

    @Override
    protected <OPN extends OpN> Op transformOpN(OPN op, List<Op> subOps, BiFunction<? super OPN, ? super List<Op>, ? extends Op> fallback) {
        Op result;

        boolean isAllTables = subOps.stream().allMatch(subOp -> subOp instanceof OpTable);
        if (isAllTables) {
            Op tmp = op.copy(subOps);
            result = exec(tmp);
        } else {
            result = fallback.apply(op, subOps);
        }

        return result;
    }

    @Override
    protected <OP2 extends Op2> Op transformOp2(OP2 op, Op left, Op right, TriFunction<? super OP2, ? super Op, ? super Op, ? extends Op> fallback) {
        Op result;

        if (left instanceof OpTable && right instanceof OpTable) {
            Op tmp = op.copy(left, right);
            result = exec(tmp);
        } else {
            result = fallback.apply(op, left, right);
        }

        return result;
    }

    @Override
    protected <OP1 extends Op1> Op transformOp1(OP1 op, Op subOp, BiFunction<? super OP1, ? super Op, ? extends Op> fallback) {
        Op result = null;

        if (subOp instanceof OpTable) {
            Op tmp = op.copy(subOp);
            result = exec(tmp);
        } else {
            result = fallback.apply(op, subOp);
        }

        return result;
    }
}
