package org.aksw.jena_sparql_api.collection.observable;

import org.apache.jena.sparql.core.DatasetGraph;

public class ObservableDatasetGraphImpl
//    implements ObservableDatasetGraph
{
    protected DatasetGraph delegate;

    public ObservableDatasetGraphImpl(DatasetGraph delegate) {
        super();
        this.delegate = delegate;
    }

    // FIXME Implement

    public static ObservableDatasetGraph decorate(DatasetGraph delegate) {
        return null;
    }
}
