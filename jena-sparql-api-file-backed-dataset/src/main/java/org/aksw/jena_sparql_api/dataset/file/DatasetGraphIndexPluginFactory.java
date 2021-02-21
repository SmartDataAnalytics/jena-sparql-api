package org.aksw.jena_sparql_api.dataset.file;

public interface DatasetGraphIndexPluginFactory {
    DatasetGraphIndexPlugin create(DatasetGraphWithSync graphWithSync);
}
