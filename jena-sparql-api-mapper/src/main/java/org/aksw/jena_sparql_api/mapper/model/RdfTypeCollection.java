package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeComplexBase;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;

public class RdfTypeCollection
    extends RdfTypeComplexBase
{
    protected Node predicate;
    protected Class<?> collectionClass;

    public RdfTypeCollection(Class<?> collectionClass, Node predicate) {
        super();
        this.predicate = predicate;
    }

//    public RdfTypeCollection(RdfTypeFactory typeFactory, Class<?> collectionClass, Node predicate) {
//        super(typeFactory);
//        this.predicate = predicate;
//    }

    @Override
    public Class<?> getEntityClass() {
        return collectionClass;
    }

    @Override
    public Node getRootNode(Object obj) {
        throw new RuntimeException(this.getClass().getSimpleName() + " does not have an RDF identity of its own; as this is inherited from the owning entity");
    }

//    @Override
//    public Object createJavaObject(Node node) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    @Override
    public Object createJavaObject(Node node, Graph graph) {
        return null;
    }

    
    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    @Override
    public void populateEntity(RdfPersistenceContext persistenceContext,
            Object entity, Node subject, Graph inGraph, Consumer<Triple> sink) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void emitTriples(
            RdfEmitterContext emitterContext, Object entity, Node subject,
            Graph shapeGraph, Consumer<Triple> sink) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public boolean hasIdentity() {
		return false;
	}

//    @Override
//    public void exposeTypeDeciderShape(ResourceShapeBuilder rsb) {
//    }
//
//    @Override
//    public Collection<RdfType> getApplicableTypes(Resource resource) {
//        return Collections.emptySet();
//    }
}
