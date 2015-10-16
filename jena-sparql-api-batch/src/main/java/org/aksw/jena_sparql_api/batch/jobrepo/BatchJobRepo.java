package org.aksw.jena_sparql_api.batch.jobrepo;

public interface BatchJobRepo {
    public void register(String name, String data, String mimetype);
    public void lookup(String name);
}
