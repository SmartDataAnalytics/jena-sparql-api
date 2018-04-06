package org.aksw.jena_sparql_api.core.service;

import org.apache.jena.rdfconnection.RDFConnection;

import com.google.common.util.concurrent.Service;

public interface SparqlBasedService
    extends Service
{
    RDFConnection createDefaultConnection();
}
