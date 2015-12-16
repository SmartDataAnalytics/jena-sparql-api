package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.atlas.lib.Sink;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.google.common.base.Defaults;
import com.google.common.collect.Iterables;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class RdfPopulatorPropertySingle
    extends RdfPopulatorPropertyBase
{
    public RdfPopulatorPropertySingle(String propertyName, Node predicate, RdfType targetRdfType) { // String fetchMode) {
        super(propertyName, predicate, targetRdfType);
    }

    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Graph out, Object entity, Node subject) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
        Object value = beanWrapper.getPropertyValue(propertyName);

        if(value != null) {

            Node o = targetRdfType.getRootNode(value);

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



        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);

        // We cannot set property values of primitive types to null
        Class<?> valueType = beanWrapper.getPropertyType(propertyName);
        if(value == null && valueType.isPrimitive()) {
            value = Defaults.defaultValue(valueType);
        }
        beanWrapper.setPropertyValue(propertyName, value);
    }

    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        shapeBuilder.outgoing(predicate);
//		ResourceShapeBuilder targetShape = shapeBuilder.outgoing(predicate);

//		if("eager".equals(fetchMode)) {
//			targetRdfType.build(targetShape);
//		}
    }

//	@Override
//	public Object readPropertyValue(Graph graph, Node subject) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
