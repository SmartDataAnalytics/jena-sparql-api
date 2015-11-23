package org.aksw.jena_sparql_api.mapper.model;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContext;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class RdfPopulatorPropertySingle
	extends RdfPopulatorPropertyBase
{
	public RdfPopulatorPropertySingle(String propertyName, Node predicate, RdfType targetRdfType) { // String fetchMode) {
		super(propertyName, predicate, targetRdfType);
	}

    @Override
    public void emitTriples(Graph out, Object obj, Node subject) {
        Node o = targetRdfType.getRootNode(obj);

//        Triple tmp = RelationUtils.extractTriple(relation);
//        Node p = tmp.getPredicate();

        //Quad t = new Quad(Quad.defaultGraphIRI, subject, p, o);
        Triple t = new Triple(subject, predicate, o);
        out.add(t);

//        if(!out.contains(t)) {
//
//            targetRdfType.writeGraph(out, obj);
//        }
    }

	@Override
	public void populateBean(RdfPopulationContext populationContext, Object bean, Graph graph, Node subject) {
//		Class<?> beanClass = bean.getClass();
//		RdfType rdfType = populationContext.forJavaType(beanClass);
//		RdfClass rdfClass = (RdfClass)rdfType;
//		RdfPropertyDescriptor propertyDescriptor = rdfClass.getPropertyDescriptors(propertyName);
//		RdfType targetRdfType = propertyDescriptor.getRdfType();

		List<Node> objects = GraphUtil.listObjects(graph, subject, predicate).toList();
		Node node = Iterables.getFirst(objects, null);

		Object value = populationContext.objectFor(targetRdfType, node);//rdfType.createJavaObject(node);

		BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
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
