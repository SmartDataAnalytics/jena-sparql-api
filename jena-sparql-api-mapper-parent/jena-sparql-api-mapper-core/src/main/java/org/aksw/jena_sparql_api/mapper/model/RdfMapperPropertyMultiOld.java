package org.aksw.jena_sparql_api.mapper.model;

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
//public class RdfMapperPropertyMulti
//    extends RdfMapperPropertyBase
//{
//    public RdfMapperPropertyMulti(PropertyOps propertyOps, Node predicate, RdfType targetRdfType, BiFunction<Object, Object, Node> createTargetNode) {
//        super(propertyOps, predicate, targetRdfType, createTargetNode);
//    }
//
//
//    //RdfPersistenceContext persistenceContext, 
//    @Override
//    public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Graph shapeGraph, Consumer<Triple> out) {
//
//        //BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
//        Collection<?> items = (Collection<?>)propertyOps.getValue(entity);//beanWrapper.getPropertyValue(propertyName);
//
//        for(Object item : items) {
//            Node o = targetRdfType.getRootNode(item);
//            Triple t = new Triple(subject, predicate, o);
//
//            out.accept(t);
//
//
//            //emitterContext.add(item, entity, propertyOps.getName());
//
////	        if(!out.contains(t)) {
////
////	            targetRdfType.writeGraph(out, item);
////	        }
//        }
//    }
//
//    /**
//     * TODO The collection
//     * @param entity
//     * @param propertyName
//     * @return
//     */
//    public static Object getOrCreateBean(Object entity, PropertyOps propertyOps) {
//        //BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
//        Object result = propertyOps.getValue(entity); //beanWrapper.getPropertyValue(propertyName);
//
//        if(result == null) {
//
//            //PropertyDescriptor pd = beanWrapper.getPropertyDescriptor(propertyName);
//            Class<?> collectionType = propertyOps.getType(); //pd.getPropertyType();
//
//            try {
//                result = collectionType.newInstance();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            //beanWrapper.setPropertyValue(propertyName, result);
//            propertyOps.setValue(entity, result);
//        }
//        return result;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void populateEntity(RdfPersistenceContext populationContext, Object bean, Graph graph, Node subject, Consumer<Triple> outSink) {
//        // Creates a collection under the given property
//        Collection<? super Object> collection = (Collection<? super Object>)getOrCreateBean(bean, propertyOps);
//
//        for(Triple t : graph.find(subject, predicate, Node.ANY).toSet()) {
//            outSink.accept(t);
//
//            Node o = t.getObject();
//        //List<Node> os = GraphUtil.listObjects(graph, subject, predicate).toList();
//
//        //for(Node o : os) {
//            //TypedNode typedNode = new TypedNode(targetRdfType, o);
//            Class<?> valueClass = propertyOps.getClass();
//            Object value = populationContext.entityFor(valueClass, o, () -> targetRdfType.createEntity(o, graph));
//            //Object value = rdfType.createJavaObject(o);
//            collection.add(value);
//        }
//    }
//
//
//    @Override
//    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
//        shapeBuilder.out(predicate);
////		ResourceShapeBuilder targetShape = shapeBuilder.outgoing(predicate);
////
////		if("eager".equals(fetchMode)) {
////			targetRdfType.build(targetShape);
////		}
//    }
//
//}
