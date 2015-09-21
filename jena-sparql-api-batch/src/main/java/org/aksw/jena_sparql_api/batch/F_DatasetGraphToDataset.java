package org.aksw.jena_sparql_api.batch;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

class F_DatasetGraphToDataset
    implements Function<DatasetGraph, Dataset>
{
    @Override
    public Dataset apply(DatasetGraph datasetGraph) {
        Dataset result = DatasetFactory.create(datasetGraph);
        return result;
    }

    public static final F_DatasetGraphToDataset fn = new F_DatasetGraphToDataset();
}