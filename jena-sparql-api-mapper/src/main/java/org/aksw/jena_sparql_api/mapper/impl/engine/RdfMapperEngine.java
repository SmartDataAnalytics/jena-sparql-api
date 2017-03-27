package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Prologue;

public interface RdfMapperEngine {

    Prologue getPrologue();

    RdfTypeFactory getRdfTypeFactory();

    //<T> LookupService<Node, T> getLookupService(Class<T> clazz);
    <T> T find(Class<T> clazz, Node rootNode);

    //List<T> list(Class<T> clazz);
    <T> List<T> list(Class<T> clazz, Concept concept);
    
    
    <T> T merge(T entity);
    <T> T merge(T entity, Node node);

    void emitTriples(Graph outGraph, Object entity);
    void emitTriples(Graph outGraph, Object entity, Node node);
}
