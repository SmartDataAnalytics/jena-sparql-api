package org.aksw.jena_sparql_api.ext.virtuoso;

import org.aksw.jena_sparql_api.core.connection.TransactionalTmp;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFDatasetAccessConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public abstract class RDFDatasetAccessConnectionBase
    implements RDFDatasetAccessConnection, TransactionalTmp
{    
    protected abstract SparqlQueryConnection getQueryConnection();
    
    @Override
    public void begin(ReadWrite readWrite) {
        getQueryConnection().begin(readWrite);
    }

    @Override
    public void commit() {
        getQueryConnection().commit();
    }

    @Override
    public void abort() {
        getQueryConnection().abort();
    }

    @Override
    public void end() {
        getQueryConnection().end();
    }

    @Override
    public boolean isInTransaction() {
        boolean result = getQueryConnection().isInTransaction();
        return result;
    }

    @Override
    public Model fetch(String graphName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model fetch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dataset fetchDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        getQueryConnection().close();
        boolean result = true;
        return result;
    }

    @Override
    public void close() {
        getQueryConnection().close();
    }
}
