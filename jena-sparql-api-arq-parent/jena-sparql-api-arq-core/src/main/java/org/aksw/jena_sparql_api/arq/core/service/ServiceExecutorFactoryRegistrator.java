package org.aksw.jena_sparql_api.arq.core.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ServiceExecutorFactoryRegistrator {
    public static final Symbol SERVICE_EXECUTORS = Symbol.create("serviceExecutors");

    public static List<ServiceExecutorFactory> getRegistrations(Context cxt) {
        List<ServiceExecutorFactory> result = cxt.get(SERVICE_EXECUTORS);

        return result;
    }

    public static List<ServiceExecutorFactory> getOrCreateRegistrations(Context cxt) {
        List<ServiceExecutorFactory> result = getRegistrations(cxt);

        if (result == null) {
            result = new ArrayList<>();
            cxt.put(SERVICE_EXECUTORS, result);
        }

        return result;
    }

    public static void register(Context cxt, ServiceExecutorFactory factory) {
        List<ServiceExecutorFactory> factories = getOrCreateRegistrations(cxt);

        if (!factories.contains(factory)) {
            factories.add(factory);
        }
    }
}
