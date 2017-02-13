package org.aksw.jena_sparql_api.mapper.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.impl.type.EntityFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PlaceholderInfo;
import org.aksw.jena_sparql_api.mapper.impl.type.PopulationTask;
import org.aksw.jena_sparql_api.mapper.impl.type.ResourceFragment;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class RdfMapperPropertySingle
    extends RdfMapperPropertyBase
{
    public RdfMapperPropertySingle(
            PropertyOps propertyOps,
            Property predicate,
            RdfType targetRdfType,
            BiFunction<Object, Object, Node> createTargetIri) { // String fetchMode) {
        super(propertyOps, predicate, targetRdfType, createTargetIri);
    }

    @Override
    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {
    	// TODO Auto-generated method stub
    	super.exposeFragment(out, priorState, entity);
    }
//    
//    @Override
//    public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Graph shapeGraph, Consumer<Triple> outSink) {
//        Object value = propertyOps.getValue(entity);
//
//        if(value != null) {
//            Supplier<Node> defaultNodeGenerator = createTargetNode == null
//                    ? null
//                    : () -> createTargetNode.apply(entity, value);
//
//            Node o = emitterContext.requestResolution(value);//, targetRdfType, defaultNodeGenerator);
//            Triple t = new Triple(subject, predicate, o);
//            outSink.accept(t);
//            
//            // maybe we should write triples to the emitter context, as references
//            // need to be resolved anyway
//            //emitterContext.accept(t);
//        }
//    }

    @Override
    public void populate(EntityFragment out, Resource shape, Object entity) {
    	Statement stmt = shape.getProperty(predicate);
    	RDFNode o = stmt == null ? null : stmt.getObject();

    	List<PlaceholderInfo> pis = Arrays.asList(new PlaceholderInfo(null, targetRdfType, entity, null, propertyOps, null, null, this));

    	//out.getPropertyInfos().put(key, value);
    	out.getTasks().add(new PopulationTask() {
			@Override
			public List<PlaceholderInfo> getPlaceholders() {
				return pis;
			}

			@Override
			public Collection<PopulationTask> resolve(List<Object> resolutions) {
				propertyOps.setValue(entity, resolutions.get(0));
				return Collections.emptyList();
			}    		
    	});
    }
//    @Override
//    public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Graph inGraph, Node subject, Consumer<Triple> outSink) {
//        List<Triple> triples = inGraph.find(subject, predicate, Node.ANY).toList();
//
//        Triple t = Iterables.getFirst(triples, null);
//
//        Node node;
//        if(t != null) {
//            node = t.getObject();
//            outSink.accept(t);
//        } else {
//            node = null;
//        }
//
//        if(node == null) {
//        	if(createTargetNode != null) {
//	        	Object childEntity = propertyOps.getValue(entity);
//	        	if(childEntity != null) {
//	        		node = createTargetNode.apply(entity, childEntity);
//	        	}
//        	}
//        }
//        
//        if(node != null) {
//        	persistenceContext.requestResolution(propertyOps, entity, node);
//        }
// 
//        //persistenceContext.requestResolution(entity, propertyOps, subject, rdfType);
//
//               
////        Object value = node == null
////                ? null
////                : persistenceContext.entityFor(new TypedNode(targetRdfType, node))
////                ;//rdfType.createJavaObject(node);
////
////
////        // We cannot set property values of primitive types to null
////        Class<?> valueType = propertyOps.getType();
////        if(value == null && valueType.isPrimitive()) {
////            value = Defaults.defaultValue(valueType);
////        }
////        propertyOps.setValue(entity, value);
//    }
//
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
