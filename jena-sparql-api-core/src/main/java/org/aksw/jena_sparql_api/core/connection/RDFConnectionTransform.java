package org.aksw.jena_sparql_api.core.connection;

import java.util.function.Function;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFConnectionTransform
    extends Function<RDFConnection, RDFConnection>
{
}
