package org.aksw.jena_sparql_api.update;

import java.util.Collection;

import org.aksw.jena_sparql_api.core.DatasetListener;

public interface DatasetListenable {
    Collection<DatasetListener> getDatasetListeners();
}
