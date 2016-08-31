package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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

//    class PropertyValueContext {
//        Node subject;
//        Object entity;
//        Object value;
//    }

    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Object entity, Node subject, Consumer<Triple> outSink) {
        //targetRdfType.getTypeFactory().forJavaType(targetRdfType.getEntityClass()).get
        Object value = propertyOps.getValue(entity);
                
        
        //Object value = targetRdfType.get
        //        BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
//        Object value = beanWrapper.getPropertyValue(propertyName);

        if(value != null) {
           
            
            // createTargetNode would have to consult the persistence context
            // for whether there is any target node for the given entity
 
            /**
             * The main problem here is, how to obtain a node that corresponds to the value.
             * If the entity being emitted here has been populated, we can obtain its value directly (we just look up the triple (subject, property, ?x))
             * However, without population, we could e.g. obtain a placeholder resource (e.g. a blank node) and mark it for later resolution.
             * At resolution, the blank nodes are replaced with their proper URIs.
             * 
             * 
             * 
             * Yet, this should not have to be done here, but this should be handled by the emitterContext.
             * 
             * The emitterContext can then be 'resolved' by the engine, which will update the persistenceContext accordingly.
             * 
             * 
             */
            
            
            //Note: By default, createTargetNode delegates to targetRdfType.getRootNode
            // SNode o = targetRdfType.getRootNode(value);
            Node o = createTargetNode.apply(entity, value);
            
            //emitterContext.add(value, entity, propertyOps.getName());
            //persistenceContext.getFrontier().add(item);

            
            if(o == null) {
                //System.out.println("HACK for testing - remove it! should throw exception instead");
                throw new RuntimeException("Failed RDF node conversion for " + value.getClass() + ": " + value);
                //o = subject;
            }
       
            
    //        Triple tmp = RelationUtils.extractTriple(relation);
    //        Node p = tmp.getPredicate();

            //Quad t = new Quad(Quad.defaultGraphIRI, subject, p, o);
            Triple t = new Triple(subject, predicate, o);
            outSink.accept(t);
            
            // TODO If we allocated a URI for an entity, we need to notify the persistenceContext about it
            // or conversely, we need to request an entity's node from the peristenceContext
            
            //persistenceContext.entityFor(new TypedNode(targetRdfType, o))
            emitterContext.add(value, entity, propertyOps.getName());
            //emitterContext.add(o, entity);
        }

        //RdfPopulationContext emitterContext;
        //emitterContext.g
        //emitterContext.$(value, obj, propertyName);
//        if(!out.contains(t)) {
//
//            targetRdfType.writeGraph(out, obj);
//        }
    }

    @Override
    public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Graph inGraph, Node subject, Consumer<Triple> outSink) {
//		Class<?> beanClass = bean.getClass();
//		RdfType rdfType = populationContext.forJavaType(beanClass);
//		RdfClass rdfClass = (RdfClass)rdfType;
//		RdfPropertyDescriptor propertyDescriptor = rdfClass.getPropertyDescriptors(propertyName);
//		RdfType targetRdfType = propertyDescriptor.getRdfType();
        List<Triple> triples = inGraph.find(subject, predicate, Node.ANY).toList();

        Triple t = Iterables.getFirst(triples, null);

        Node node;
        if(t != null) {
            node = t.getObject();
            outSink.accept(t);
        } else {
            node = null;
        }

        Object value = node == null
                ? null
                : persistenceContext.entityFor(new TypedNode(targetRdfType, node))
                ;//rdfType.createJavaObject(node);


        // We cannot set property values of primitive types to null
        Class<?> valueType = propertyOps.getType();
        if(value == null && valueType.isPrimitive()) {
            value = Defaults.defaultValue(valueType);
        }
        propertyOps.setValue(entity, value);
    }

    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        shapeBuilder.outgoing(predicate);
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
