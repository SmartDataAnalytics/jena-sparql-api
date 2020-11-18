package org.aksw.jena_sparql_api.io.filter.sys;

import java.nio.file.Path;

import org.aksw.jena_sparql_api.io.endpoint.Destination;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFilter;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFromFile;
import org.aksw.jena_sparql_api.io.endpoint.FilterConfig;
import org.aksw.jena_sparql_api.io.endpoint.FilterEngine;
import org.aksw.jena_sparql_api.io.endpoint.InputStreamSupplier;

import io.reactivex.rxjava3.core.Single;

public class FilterEngineFromSysFunction
    implements FilterEngine
{
    protected SysCallFn cmdFactory;

    public FilterEngineFromSysFunction(SysCallFn cmdFactory) {
        this.cmdFactory = cmdFactory;
    }

    @Override
    public FilterConfig forInput(Path in) {
        return new FilterExecutionFromSysFunction(cmdFactory, new DestinationFromFile(in));
    }

    @Override
    public FilterConfig forInput(InputStreamSupplier in) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilterConfig forInput(FilterConfig in) {
        return new FilterExecutionFromSysFunction(cmdFactory, new DestinationFilter(in));
    }

    @Override
    public FilterConfig forInput(Single<Path> futurePath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilterConfig forInput(Destination destination) {
        return new FilterExecutionFromSysFunction(cmdFactory, destination);
    }

}
