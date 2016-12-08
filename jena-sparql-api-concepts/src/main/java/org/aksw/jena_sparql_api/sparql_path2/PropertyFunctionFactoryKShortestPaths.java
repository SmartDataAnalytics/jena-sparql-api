package org.aksw.jena_sparql_api.sparql_path2;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

public class PropertyFunctionFactoryKShortestPaths
    implements PropertyFunctionFactory
{
    protected Function<SparqlService, SparqlKShortestPathFinder> serviceToPathFinder;

    public PropertyFunctionFactoryKShortestPaths(Function<SparqlService, SparqlKShortestPathFinder> serviceToPathFinder) {
        this.serviceToPathFinder = serviceToPathFinder;
    }

    @Override
    public PropertyFunction create(String uri) {
        PropertyFunctionKShortestPaths result = PropertyFunctionKShortestPaths.DEFAULT_IRI.equals(uri)
                ? new PropertyFunctionKShortestPaths(serviceToPathFinder)
                : null
                ;

        return result;
    }
}
