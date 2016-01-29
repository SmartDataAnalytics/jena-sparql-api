package org.aksw.jena_sparql_api.mapper.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.model.RdfType;

import org.apache.jena.graph.Node;

//public class RdfPopulationContextImpl
//	implements RdfPopulationContext
//{
//	protected IdentityHashMap<Object, Map<String, Object>> beanStates = new IdentityHashMap<Object, Map<String, Object>>();
//
//	protected Set<Node> open = new HashSet<Node>();
//
//	@Override
//	public Object objectFor(RdfType rdfType, Node node) {
//		Object result;
//
//		// Check if there is already a java object for the given class with the given id
//		result = rdfType.createJavaObject(node);
//
//		Map<String, Object> beanState = new HashMap<String, Object>();
//		beanStates.put(result, beanState);
//
//		return result;
//	}
//
//	public void checkManaged(Object bean) {
//		if(!isManaged(bean)) {
//			throw new RuntimeException("Bean was expected to be managed: " + bean);
//		}
//	}
//
//	/**
//	 *
//	 * @param bean
//	 * @return
//	 */
//	public Map<String, Object> getBeanState(Object bean) {
//		checkManaged(bean);
//
//		Map<String, Object> result = beanStates.get(bean);
////		if(result == null) {
////			result = Collections.emptyMap();
////		}
//		return result;
//	}
//
//	public boolean isManaged(Object bean) {
//		boolean result = beanStates.containsKey(bean);
//		return result;
//	}
//
//
//	/**
//	 * Convenience accessors
//	 *
//	 * @param bean
//	 * @return
//	 */
//
//	public boolean isPopulated(Object bean) {
//		Object o = beanStates.get(bean).get("populated");
//		boolean result = o != null && !Boolean.FALSE.equals(o);
//		return result;
//	}
//
//	public void setPopulated(Object bean, boolean value) {
//		beanStates.get(bean).put("populated", true);
//	}
//}
