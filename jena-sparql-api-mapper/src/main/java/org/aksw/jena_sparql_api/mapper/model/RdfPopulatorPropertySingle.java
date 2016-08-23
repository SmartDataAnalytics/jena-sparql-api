package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.base.Defaults;
import com.google.common.collect.Iterables;

public class RdfPopulatorPropertySingle
    extends RdfPopulatorPropertyBase
{
    public RdfPopulatorPropertySingle(PropertyOps propertyOps, Node predicate, RdfType targetRdfType) { // String fetchMode) {
        super(propertyOps, predicate, targetRdfType);
    }

    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Graph out, Object entity, Node subject) {
        //targetRdfType.getTypeFactory().forJavaType(targetRdfType.getEntityClass()).get
        Object value = propertyOps.getValue(entity);
        
        //Object value = targetRdfType.get
        //        BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
//        Object value = beanWrapper.getPropertyValue(propertyName);

        if(value != null) {

            Node o = targetRdfType.getRootNode(value);
            if(o == null) {
                throw new RuntimeException("Failed RDF node conversion for " + value.getClass() + ": " + value);
            }
            
            
    //        Triple tmp = RelationUtils.extractTriple(relation);
    //        Node p = tmp.getPredicate();

            //Quad t = new Quad(Quad.defaultGraphIRI, subject, p, o);
            Triple t = new Triple(subject, predicate, o);
            out.add(t);
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
    public void populateEntity(RdfPersistenceContext persistenceContext, Object bean, Graph graph, Node subject, Sink<Triple> outSink) {
//		Class<?> beanClass = bean.getClass();
//		RdfType rdfType = populationContext.forJavaType(beanClass);
//		RdfClass rdfClass = (RdfClass)rdfType;
//		RdfPropertyDescriptor propertyDescriptor = rdfClass.getPropertyDescriptors(propertyName);
//		RdfType targetRdfType = propertyDescriptor.getRdfType();
        List<Triple> triples = graph.find(subject, predicate, Node.ANY).toList();

        Triple t = Iterables.getFirst(triples, null);

        Node node;
        if(t != null) {
            node = t.getObject();
            outSink.send(t);
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
        propertyOps.setValue(bean, value);
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
