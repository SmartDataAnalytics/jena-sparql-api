package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeComplexBase;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.atlas.lib.Sink;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class RdfTypeCollection
    extends RdfTypeComplexBase
{
    protected Node predicate;
    protected Class<?> collectionClass;

    public RdfTypeCollection(RdfTypeFactory typeFactory, Class<?> collectionClass, Node predicate) {
        super(typeFactory);
        this.predicate = predicate;
    }

    @Override
    public Class<?> getEntityClass() {
        return collectionClass;
    }

    @Override
    public Node getRootNode(Object obj) {
        throw new RuntimeException(this.getClass().getSimpleName() + " does not have an RDF identity of its own; as this is inherited from the owning entity");
    }

    @Override
    public Object createJavaObject(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    @Override
    public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Graph inGraph,
            Sink<Triple> outSink) {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Graph out,
            Object obj) {
        // TODO Auto-generated method stub

    }

}
