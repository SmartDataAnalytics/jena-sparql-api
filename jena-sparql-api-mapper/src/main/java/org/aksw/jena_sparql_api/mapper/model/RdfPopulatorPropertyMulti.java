package org.aksw.jena_sparql_api.mapper.model;

import java.beans.PropertyDescriptor;
import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.atlas.lib.Sink;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * Mapping for multiple occurrences of the same RDF predicate to
 * a collection typed bean property.
 *
 * !!! Note that the targetRdfType of such a property is that of the items !!!
 * Example:
 *
 * <pre>
 * class MyClass {
 *   \@Iri("my:property)
 *   \@MultiValuedProperty
 *   List&lt;String&gt; names;
 * }
 * </pre>
 * The targetRdfType in this case is String.
 *
 * If however @MultiValuedProperty is not present, by default an RDF Seq will be used which will be assigned its own IRI and thus identity.
 *
 *
 * TODO Clarify relation to indexed properties
 *
 * @author raven
 *
 */
public class RdfPopulatorPropertyMulti
    extends RdfPopulatorPropertyBase
{
    public RdfPopulatorPropertyMulti(String propertyName, Node predicate, RdfType targetRdfType) {
        super(propertyName, predicate, targetRdfType);
    }


    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Graph out, Object entity, Node subject) {

        BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
        Collection<?> items = (Collection<?>)beanWrapper.getPropertyValue(propertyName);

        for(Object item : items) {
            Node o = targetRdfType.getRootNode(item);
            Triple t = new Triple(subject, predicate, o);

            out.add(t);


            emitterContext.add(item, entity, propertyName);

//	        if(!out.contains(t)) {
//
//	            targetRdfType.writeGraph(out, item);
//	        }
        }
    }

    /**
     * TODO The collection
     * @param entity
     * @param propertyName
     * @return
     */
    public static Object getOrCreateBean(Object entity, String propertyName) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
        Object result = beanWrapper.getPropertyValue(propertyName);

        if(result == null) {

            PropertyDescriptor pd = beanWrapper.getPropertyDescriptor(propertyName);
            Class<?> collectionType = pd.getPropertyType();

            try {
                result = collectionType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            beanWrapper.setPropertyValue(propertyName, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void populateEntity(RdfPersistenceContext populationContext, Object bean, Graph graph, Node subject, Sink<Triple> outSink) {
        // Creates a collection under the given property
        Collection<? super Object> collection = (Collection<? super Object>)getOrCreateBean(bean, propertyName);

        for(Triple t : graph.find(subject, predicate, Node.ANY).toSet()) {
            outSink.send(t);

            Node o = t.getObject();
        //List<Node> os = GraphUtil.listObjects(graph, subject, predicate).toList();

        //for(Node o : os) {
            TypedNode typedNode = new TypedNode(targetRdfType, o);
            Object value = populationContext.entityFor(typedNode);
            //Object value = rdfType.createJavaObject(o);
            collection.add(value);
        }
    }


    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        shapeBuilder.outgoing(predicate);
//		ResourceShapeBuilder targetShape = shapeBuilder.outgoing(predicate);
//
//		if("eager".equals(fetchMode)) {
//			targetRdfType.build(targetShape);
//		}
    }
}
