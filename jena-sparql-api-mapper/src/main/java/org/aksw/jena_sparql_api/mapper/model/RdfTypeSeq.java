package org.aksw.jena_sparql_api.mapper.model;

import java.lang.reflect.Type;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

/**
 * A type that
 *
 * @author raven
 *
 */
public class RdfTypeSeq
    implements RdfType
{
    private RdfType itemRdfType;

    public PropertyRelation createRelation() {
        return RdfSeqUtils.seqRelation;
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

    @Override
    public Class<?> getTargetClass() {
        // TODO Auto-generated method stub
        return null;
    }
}
