package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class RdfTypePrimitive
    implements RdfType
{
    protected RDFDatatype rdfDatatype;

    public RdfTypePrimitive(RDFDatatype rdfDatatype) {
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Class<?> getTargetClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void build(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setValues(Object targetObj, DatasetGraph datasetGraph) {
        // TODO Auto-generated method stub

    }

    @Override
    public DatasetGraph createDatasetGraph(Object obj, Node g) {
        // TODO Auto-generated method stub
        return null;
    }

}
