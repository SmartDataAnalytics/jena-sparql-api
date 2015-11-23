package org.aksw.jena_sparql_api.mapper.impl.engine;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public interface RdfMapperEngine {
	RdfTypeFactory getRdfTypeFactory();

	//<T> LookupService<Node, T> getLookupService(Class<T> clazz);
	public <T> T find(Class<T> clazz, Node rootNode);

	<T> T merge(T entity);

	void emitTriples(Graph outGraph, Object entity);
}
