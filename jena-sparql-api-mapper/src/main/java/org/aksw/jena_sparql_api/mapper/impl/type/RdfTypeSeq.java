package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.model.RdfSeqUtils;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

/**
 * A type that
 *
 * @author raven
 *
 */
public class RdfTypeSeq
    extends RdfTypeComplexBase
{
    public RdfTypeSeq(RdfTypeFactory typeFactory, RdfType itemRdfType) {
        super(typeFactory);
        this.itemRdfType = itemRdfType;
    }

    private RdfType itemRdfType;

    public PropertyRelation createRelation() {
        return RdfSeqUtils.seqRelation;
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<?> getBeanClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getRootNode(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object createJavaObject(Node node) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void emitTriples(RdfEmitterContext emitterContext, Graph out, Object obj) {
        // TODO Auto-generated method stub

    }

    @Override
    public void populateBean(RdfPersistenceContext persistenceContext, Object targetObj, Graph graph) {
        // TODO Auto-generated method stub

    }

}
