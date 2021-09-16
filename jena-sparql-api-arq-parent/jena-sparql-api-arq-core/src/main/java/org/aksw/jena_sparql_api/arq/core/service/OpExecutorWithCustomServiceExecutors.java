package org.aksw.jena_sparql_api.arq.core.service;

import java.util.List;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;


/**
 * Subclass of OpExecutor for which a {@link List} of {@link ServiceExecutor}s
 * can be supplied via the context.
 *
 * Custom executor implementations should prefer deriving from this class
 * over using OpExecutor directly.
 *
 * @author Claus Stadler
 *
 */
public class OpExecutorWithCustomServiceExecutors
    extends OpExecutor
{
    // OpExecutorWithCustomServiceExecutors::new already fulfills the OpExecutorFactory interface contract
    // public static final OpExecutorFactory FACTORY = OpExecutorWithCustomServiceExecutors::new;


    public OpExecutorWithCustomServiceExecutors(ExecutionContext execCxt) {
        super(execCxt);
    }

    @Override
    public QueryIterator execute(OpService opService, QueryIterator input) {
        return new QueryIterServiceWithCustomExecutors(input, opService, execCxt);
    }

    /** Register this class as the OpExecutor in the given context */
    public static void register(Context context) {
        QC.setFactory(context, OpExecutorWithCustomServiceExecutors::new);
    }
}
