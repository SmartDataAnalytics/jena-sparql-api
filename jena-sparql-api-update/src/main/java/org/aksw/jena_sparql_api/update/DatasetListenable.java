package org.aksw.jena_sparql_api.update;

import java.util.Collection;

public interface DatasetListenable {
    Collection<DatasetListener> getDatasetListeners();
}
