package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.model.TypeDecider;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Prologue;

public interface RdfMapperEngine
{
    SparqlService getSparqlService();

    Prologue getPrologue();

    RdfTypeFactory getRdfTypeFactory();

    TypeDecider getTypeDecider();

    <T> T find(Class<T> clazz, Node rootNode);

    <T> List<T> list(Class<T> clazz, Concept concept);


    <T> T merge(T entity);
    <T> T merge(T entity, Node node);


    void remove(Object entity);
    void remove(Node node, Class<?> clazz);

    String getIri(Object entity);

    //Map<Node, RDFNode> fetch(ShapeExposable shapeSupplier, Collection<Node> nodes);


    /**
     * Creates a path resolver starting from the given entityClass.
     * The rootVar
     *
     * @param javaClass
     * @param rootVar
     * @return
     */
    PathResolver createResolver(Class<?> entityClass);
}
