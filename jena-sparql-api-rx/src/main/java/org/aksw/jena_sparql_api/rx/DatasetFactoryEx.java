package org.aksw.jena_sparql_api.rx;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;


public class DatasetFactoryEx {

    /**
     * Create a Dataset backed by LinkedHashMaps such that insert order
     * is preserved.
     * Allows retaining order of queries such as CONSTRUCT { ... } WHERE { ... } ORDER BY ?x ?y ?z
     *
     * @return
     */
    public static Dataset createInsertOrderPreservingDataset() {
        DatasetGraph datasetGraph = DatasetGraphFactoryEx.createInsertOrderPreservingDatasetGraph();
        Dataset result = DatasetFactory.wrap(datasetGraph);
        return result;
    }

    public static Dataset createInsertOrderPreservingDataset(Iterable<Quad> it) {
        Dataset result = createInsertOrderPreservingDataset();
        DatasetGraph dg = result.asDatasetGraph();
        it.forEach(dg::add);
        return result;
    }
}
