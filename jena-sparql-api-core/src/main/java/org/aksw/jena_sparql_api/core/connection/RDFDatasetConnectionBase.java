package org.aksw.jena_sparql_api.core.connection;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.sparql.core.Quad;

public abstract class RDFDatasetConnectionBase
    implements TransactionalTmp, RDFDatasetConnection
{
    protected SparqlQueryConnection queryConn;
    protected SparqlUpdateConnection updateConn;

    public static final Query QUERY_CONSTRUCT_SPO = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");

    public abstract void loadTriples(Stream<Triple> tripleStream);
    public abstract void loadQuads(Stream<Quad> quadStream);


    @Override
    public Model fetch(String graphName) {
        Query tmp = QUERY_CONSTRUCT_SPO.cloneQuery();
        tmp.addGraphURI(graphName);

        Model result = queryConn.queryConstruct(tmp);
        return result;
    }

    @Override
    public Model fetch() {
        Model result = queryConn.queryConstruct(QUERY_CONSTRUCT_SPO);
        return result;
    }

    @Override
    public Dataset fetchDataset() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void load(String graphName, String file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void load(String file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void load(String graphName, Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    public void load(Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    public void put(String graphName, String file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void put(String file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void put(String graphName, Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    public void put(Model model) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(String graphName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete() {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadDataset(String file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadDataset(Dataset dataset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void putDataset(String file) {
        // TODO Auto-generated method stub

    }

    @Override
    public void putDataset(Dataset dataset) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
