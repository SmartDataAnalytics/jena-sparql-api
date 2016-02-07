package org.aksw.jena_sparql_api_sparql_path2;

import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

public class PropertyFunctionFactoryKShortestPaths
    implements PropertyFunctionFactory
{
    @Override
    public PropertyFunction create(String uri) {
        System.out.println("created property function");

        PropertyFunctionKShortestPaths result = PropertyFunctionKShortestPaths.DEFAULT_IRI.equals(uri)
                ? new PropertyFunctionKShortestPaths()
                : null
                ;

        return result;
    }
}
