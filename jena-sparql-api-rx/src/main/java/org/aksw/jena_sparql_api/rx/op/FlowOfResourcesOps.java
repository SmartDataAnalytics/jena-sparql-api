package org.aksw.jena_sparql_api.rx.op;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Resource;

import io.reactivex.rxjava3.core.Flowable;

public class FlowOfResourcesOps {
   public static Flowable<Dataset> mapToDatasets(Flowable<Resource> in) {
        return in.map(r -> {
            Dataset ds = DatasetFactory.create();
            ds.addNamedModel(r.getURI(), r.getModel());
            return ds;
        });
    }
}
