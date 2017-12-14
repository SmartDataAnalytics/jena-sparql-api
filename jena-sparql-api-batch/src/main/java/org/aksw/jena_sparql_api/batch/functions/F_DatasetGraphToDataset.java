package org.aksw.jena_sparql_api.batch.functions;

import com.google.common.base.Function;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;

class F_DatasetGraphToDataset
    implements Function<DatasetGraph, Dataset>
{
    @Override
    public Dataset apply(DatasetGraph datasetGraph) {
        Dataset result = DatasetFactory.wrap(datasetGraph);
        return result;
    }

    public static final F_DatasetGraphToDataset fn = new F_DatasetGraphToDataset();
}