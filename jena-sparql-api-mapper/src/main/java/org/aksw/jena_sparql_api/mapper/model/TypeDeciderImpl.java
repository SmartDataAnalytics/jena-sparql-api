package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;


public class TypeDeciderImpl
    implements TypeDecider
{
	private static final Logger logger = LoggerFactory.getLogger(TypeDeciderImpl.class);

    protected Property typeProperty;
    protected Map<Node, Class<?>> nodeToClass;
    protected Map<Class<?>, Node> classToNode;

    
    public TypeDeciderImpl() {
    	this(RDF.type, new HashMap<>(), new HashMap<>());
    }

    public TypeDeciderImpl(Property typeProperty, Map<Node, Class<?>> nodeToClass, Map<Class<?>, Node> classToNode) {
		super();
		this.typeProperty = typeProperty;
		this.nodeToClass = nodeToClass;
		this.classToNode = classToNode;
	}

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
        if(type != null) {
        
	        Model model = outResource.getModel();
	        RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(type, model);
	
	        outResource
	            .addProperty(typeProperty, rdfNode);
        }
    }


    public static Map<Class<?>, Node> scan(String basePackage) {
    	ClassPathScanningCandidateComponentProvider provider
        	= new ClassPathScanningCandidateComponentProvider(false);
    	provider.addIncludeFilter(new AnnotationTypeFilter(RdfType.class));
    	//return provider;
    	Set<BeanDefinition> beanDefs = provider.findCandidateComponents(basePackage);
    	Map<Class<?>, Node> result = new HashMap<>();
    	for(BeanDefinition beanDef : beanDefs) {
    		String beanClassName = beanDef.getBeanClassName();
    		Class<?> beanClass;
    		try {
    			beanClass = Class.forName(beanClassName); //beanDef.getBeanClassName();
    		} catch(Exception e) {
    			logger.warn("Skipped class due to exception: " + beanClassName);
    			continue;
    		}
    		//Ann
    		RdfType rdfType = AnnotationUtils.findAnnotation(beanClass, RdfType.class);
    		Node node = NodeFactory.createURI(rdfType.value());
    		result.put(beanClass, node);
    	}

    	return result;
    }
}
