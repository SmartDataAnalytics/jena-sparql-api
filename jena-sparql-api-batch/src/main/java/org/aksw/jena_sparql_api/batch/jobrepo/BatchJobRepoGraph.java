package org.aksw.jena_sparql_api.batch.jobrepo;

import com.hp.hpl.jena.graph.Graph;

/**
 * Implementation of a job repo backed by an RDF graph
 *
 * @author raven
 *
 */
public class BatchJobRepoGraph
    implements BatchJobRepo
{
    protected Graph graph;

    @Override
    public void register(String name, String data, String mimetype) {
        // TODO Auto-generated method stub

    }

    @Override
    public void lookup(String name) {
        // TODO Auto-generated method stub

    }

}
