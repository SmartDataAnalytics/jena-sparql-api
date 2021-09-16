package org.aksw.jena_sparql_api.arq.core.service;

import java.util.List;
import java.util.function.Supplier;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.iterator.QueryIterService;
import org.apache.jena.sparql.util.Context;

public class QueryIterServiceWithCustomExecutors
    extends QueryIterService
{
    protected OpService opService;

    public QueryIterServiceWithCustomExecutors(QueryIterator input, OpService opService, ExecutionContext context) {
        super(input, opService, context);

        // TODO Sigh, Jena made this attribute package visible only...
        this.opService = opService;
    }


    @Override
    protected QueryIterator nextStage(Binding outerBinding) {
        OpService op = (OpService)QC.substitute(opService, outerBinding);

        ExecutionContext execCxt = getExecContext();
        Context cxt = execCxt.getContext();

        List<ServiceExecutorFactory> factories = ServiceExecutorFactoryRegistrator.getRegistrations(cxt);

        QueryIterator result = null;
        if (factories != null) {
            for (ServiceExecutorFactory factory : factories) {
                Supplier<QueryIterator> executor = factory.createExecutor(op, outerBinding, execCxt);
                if (executor != null) {
                    result = executor.get();
                    break;
                }
            }
        }

        if (result == null) {
            result = super.nextStage(outerBinding);
        }

        return result;
    }

}
