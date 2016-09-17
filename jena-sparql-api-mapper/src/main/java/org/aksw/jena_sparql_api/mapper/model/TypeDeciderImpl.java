package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;


public class TypeDeciderImpl
    implements TypeDecider
{
    protected Property typeProperty = RDF.type;
    protected Map<Node, Class<?>> nodeToClass;
    protected Map<Class<?>, Node> classToNode;

    
    public void addMapping(Node node, Class<?> clazz) {
        nodeToClass.put(node, clazz);
        classToNode.put(clazz, node);
    }
    
    // TODO We may want to take the type hierarchy on the RDF level into account
    // However, we should not require to rely on it
    
    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        rsb.out(typeProperty);
    }

    @Override
    public Collection<Class<?>> getApplicableTypes(Resource subject) {
        Set<Class<?>> result = subject
            .listProperties(typeProperty).toSet().stream()
            .map(stmt -> stmt.getObject().asNode())
            .map(o -> nodeToClass.get(o))
            .filter(o -> o != null)
            .collect(Collectors.toSet());
        
        return result;
    }

    @Override
    public void writeTypeTriples(Resource outResource, Object entity) {
        Class<?> clazz = entity.getClass();
        Node type = classToNode.get(clazz);
        
        Model model = outResource.getModel();
        RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(type, model);
        
        outResource
            .addProperty(typeProperty, rdfNode);
    }
}
