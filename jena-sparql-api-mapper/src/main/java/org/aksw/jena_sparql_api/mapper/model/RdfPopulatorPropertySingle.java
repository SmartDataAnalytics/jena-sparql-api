package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.base.Defaults;
import com.google.common.collect.Iterables;

public class RdfPopulatorPropertySingle
    extends RdfPopulatorPropertyBase
{
    public RdfPopulatorPropertySingle(
            PropertyOps propertyOps,
            Node predicate,
            RdfType targetRdfType,
            BiFunction<Object, Object, Node> createTargetIri) { // String fetchMode) {
        super(propertyOps, predicate, targetRdfType, createTargetIri);
    }

    @Override
    public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Graph shapeGraph, Consumer<Triple> outSink) {
        Object value = propertyOps.getValue(entity);

        if(value != null) {
            Supplier<Node> defaultNodeGenerator = createTargetNode == null
                    ? null
                    : () -> createTargetNode.apply(entity, value);

            Node o = emitterContext.requestResolution(value);//, targetRdfType, defaultNodeGenerator);
            Triple t = new Triple(subject, predicate, o);
            outSink.accept(t);
            
            // maybe we should write triples to the emitter context, as references
            // need to be resolved anyway
            //emitterContext.accept(t);
        }
    }

    @Override
    public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Graph inGraph, Node subject, Consumer<Triple> outSink) {
        List<Triple> triples = inGraph.find(subject, predicate, Node.ANY).toList();

        Triple t = Iterables.getFirst(triples, null);

        Node node;
        if(t != null) {
            node = t.getObject();
            outSink.accept(t);
        } else {
            node = null;
        }

        if(node == null) {
        	if(createTargetNode != null) {
	        	Object childEntity = propertyOps.getValue(entity);
	        	if(childEntity != null) {
	        		node = createTargetNode.apply(entity, childEntity);
	        	}
        	}
        }
        
        if(node != null) {
        	persistenceContext.requestResolution(propertyOps, entity, node);
        }
 
        //persistenceContext.requestResolution(entity, propertyOps, subject, rdfType);

               
//        Object value = node == null
//                ? null
//                : persistenceContext.entityFor(new TypedNode(targetRdfType, node))
//                ;//rdfType.createJavaObject(node);
//
//
//        // We cannot set property values of primitive types to null
//        Class<?> valueType = propertyOps.getType();
//        if(value == null && valueType.isPrimitive()) {
//            value = Defaults.defaultValue(valueType);
//        }
//        propertyOps.setValue(entity, value);
    }

    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        shapeBuilder.out(predicate);
//		ResourceShapeBuilder targetShape = shapeBuilder.outgoing(predicate);

//		if("eager".equals(fetchMode)) {
//			targetRdfType.build(targetShape);
//		}
    }

    @Override
    public String toString() {
        return "RdfPopulatorPropertySingle [propertyName=" + propertyOps.getName()
                + ", predicate=" + predicate + ", targetRdfType="
                + targetRdfType + "]";
    }

    @Override
    public PropertyOps getPropertyOps() {
        return propertyOps;
    }

//	@Override
//	public Object readPropertyValue(Graph graph, Node subject) {
//		// TODO Auto-generated method stub
//		return null;
//	}
    
    
}
