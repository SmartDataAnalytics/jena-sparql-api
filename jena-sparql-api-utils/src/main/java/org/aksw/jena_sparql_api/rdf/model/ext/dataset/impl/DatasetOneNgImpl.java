package org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetGraphOneNg;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetOneNg;
import org.apache.jena.sparql.core.DatasetImpl;

public class DatasetOneNgImpl
    extends DatasetImpl
    implements DatasetOneNg
{
    public DatasetOneNgImpl(DatasetGraphOneNg dsg) {
        super(dsg);
    }

    public static DatasetOneNg wrap(DatasetGraphOneNg dsg) {
        return new DatasetOneNgImpl(dsg);
    }

    @Override
    public String getGraphName() {
        return ((DatasetGraphOneNg)dsg).getGraphNode().getURI();
    }
}
