package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.function.Consumer;

import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

public class RdfTypeMap
    extends RdfTypeComplexBase
{
    protected EntityOps entityOps;
    
    public RdfTypeMap(RdfTypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    public Class<?> getEntityClass() {
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
        Object result = entityOps.newInstance();
        return result;
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        Node entry = NodeFactory.createURI("http://jsa.aksw.org/ontology/entry");
        Node key = NodeFactory.createURI("http://jsa.aksw.org/ontology/key");
        Node value = NodeFactory.createURI("http://jsa.aksw.org/ontology/value");
        ResourceShapeBuilder tmp = rsb
            .outgoing(entry);
        
        tmp.outgoing(key);
        tmp.outgoing(value);
    }

    @Override
    public void populateEntity(RdfPersistenceContext persistenceContext,
            Object entity, Node subject, Graph inGraph, Consumer<Triple> sink) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext,
            RdfEmitterContext emitterContext, Object entity, Node subject,
            Consumer<Triple> sink) {
        // TODO Auto-generated method stub
        
    }
    
}
