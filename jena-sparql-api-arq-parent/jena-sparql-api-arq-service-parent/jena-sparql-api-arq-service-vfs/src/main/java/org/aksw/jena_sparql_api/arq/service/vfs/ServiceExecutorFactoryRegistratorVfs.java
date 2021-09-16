package org.aksw.jena_sparql_api.arq.service.vfs;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.arq.core.service.ServiceExecutorFactory;
import org.aksw.jena_sparql_api.arq.core.service.ServiceExecutorFactoryRegistrator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

public class ServiceExecutorFactoryRegistratorVfs {
    public static final ServiceExecutorFactory FACTORY = new ServiceExecutorFactory() {
        @Override
        public Supplier<QueryIterator> createExecutor(OpService op, Binding binding, ExecutionContext execCxt) {
            Node serviceNode = op.getService();
            Entry<Path, Map<String, String>> fileSpec = ServiceExecutorFactoryVfsUtils.toPathSpec(serviceNode);

            Supplier<QueryIterator> result = fileSpec == null
                    ? null
                    : () -> ServiceExecutorFactoryVfsUtils.nextStage(op, binding, execCxt, fileSpec.getKey(), fileSpec.getValue());

            return result;
        }
    };

    public static void register(Context cxt) {
        ServiceExecutorFactoryRegistrator.register(cxt, FACTORY);
    }
}
