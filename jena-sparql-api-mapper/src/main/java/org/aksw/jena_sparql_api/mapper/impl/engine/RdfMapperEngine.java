package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.model.ShapeExposable;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Prologue;

public interface RdfMapperEngine
{
	SparqlService getSparqlService();
	
    Prologue getPrologue();

    RdfTypeFactory getRdfTypeFactory();

    //<T> LookupService<Node, T> getLookupService(Class<T> clazz);
    <T> T find(Class<T> clazz, Node rootNode);

    //List<T> list(Class<T> clazz);
    <T> List<T> list(Class<T> clazz, Concept concept);


    <T> T merge(T entity);
    <T> T merge(T entity, Node node);

    
    void remove(Object entity);
    void remove(Node node, Class<?> clazz);
    
//    void emitTriples(Graph outGraph, Object entity);
//    void emitTriples(Graph outGraph, Object entity, Node node);


    // This method should probably not go here
    Map<Node, RDFNode> fetch(ShapeExposable shapeSupplier, Collection<Node> nodes);
    
    
    public PathResolver createResolver(Class<?> javaClass);

    //PathResolver getRelation(Path path);
}
