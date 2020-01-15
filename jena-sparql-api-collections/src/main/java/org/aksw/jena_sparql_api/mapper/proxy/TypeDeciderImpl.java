package org.aksw.jena_sparql_api.mapper.proxy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDF;

import com.google.common.base.Strings;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;



public class TypeDeciderImpl
    implements TypeDecider
{
//    private static final Logger logger = LoggerFactory.getLogger(TypeDeciderImpl.class);

    protected PrefixMapping prefixMapping;
    protected Property typeProperty;
    protected Map<Node, Class<?>> nodeToClass;
    protected Map<Class<?>, Node> classToNode;

    @Override
    public String toString() {
    	return "TypeDeciderImpl ["
    			+ "typeProperty= " + typeProperty + ", "
    			+ "nodeToClass.size=" + nodeToClass.size() + ", "
    			+ "classToNode.size=" + classToNode.size() + "]";
    }
    
    public TypeDeciderImpl() {
        this(RDF.type, new HashMap<>(), new HashMap<>(), DefaultPrefixes.prefixes);
    }

    public TypeDeciderImpl(
    		Property typeProperty,
    		Map<Node, Class<?>> nodeToClass,
    		Map<Class<?>, Node> classToNode,
    		PrefixMapping prefixMapping) {
        super();
        this.typeProperty = typeProperty;
        this.nodeToClass = nodeToClass;
        this.classToNode = classToNode;
        this.prefixMapping = prefixMapping;
    }

    public synchronized void put(Class<?> clazz, Node node) {
        nodeToClass.put(node, clazz);
        classToNode.put(clazz, node);
    }

    public synchronized void putAll(Map<Class<?>, Node> map) {
        map.entrySet().forEach(e -> put(e.getKey(), e.getValue()));
    }

    public TypeDeciderImpl scan(Class<?> protoClass) {
		String basePackage = protoClass.getPackage().getName();
		
		Map<Class<?>, Node> map = scan(basePackage);
		putAll(map);
    	
    	return this;
    }
    
    // TODO We may want to take the type hierarchy on the RDF level into account
    // However, we should not require to rely on it

//    @Override
//    public void exposeShape(ResourceShapeBuilder rsb) {
//        rsb.out(typeProperty);
//    }
//
//    @Override
//    public void exposeShape(ResourceShapeBuilder rsb, Class<?> clazz) {
//        Node node = classToNode.get(clazz);
//        if(node == null) {
//            throw new RuntimeException("No corresponding concept found for class " + clazz);
//        }
//        rsb.out(typeProperty).filter(node);
//    }

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
    public void writeTypeTriples(Resource outResource, Class<?> clazz) { //Object entity) {
        //Class<?> clazz = entity.getClass();
        Node type = classToNode.get(clazz);
        if(type != null) {

            Model model = outResource.getModel();
            RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(type, model);

            outResource
                .addProperty(typeProperty, rdfNode);
        }
    }
    
	public TypeDeciderImpl registerClasses(Class<?> ... classes) {
		return registerClasses(Arrays.asList(classes));
	}
  
    public TypeDeciderImpl registerClasses(Iterable<Class<?>> classes) {
	    for(Class<?> clazz : classes) {
		    Map<Class<?>, Node> map = processClass(clazz, prefixMapping);
		    putAll(map);
	    }
	    return this;
    }


    public static Map<Class<?>, Node> scan(String basePackage) {
        Map<Class<?>, Node> result = scan(basePackage, DefaultPrefixes.prefixes);
        return result;
    }
    
//    public static Map<Class<?>, Node> scan(Class<?> clazz, PrefixMapping prefixMapping) {
//    }
    
    
    public static Map<Class<?>, Node> processClass(Class<?> clazz, PrefixMapping prefixMapping) {
    	Map<Class<?>, Node> result;
    	
    	RdfType rdfType = clazz.getAnnotation(RdfType.class);
    	RdfTypeNs rdfTypeNs = clazz.getAnnotation(RdfTypeNs.class);
		
    	// TODO It is an error to have both annotations set
    	
    	if(rdfTypeNs != null) {
            String ns = rdfTypeNs.value();
			String uri = prefixMapping.getNsPrefixURI(ns);
			if(uri == null) {
				throw new RuntimeException("Undefined prefix: " + ns + " on class " + clazz);
			}
//          if(Strings.isNullOrEmpty(iri)) {
//        	iri = "java://" + clazz.getCanonicalName();
//        }
            
			String localName = clazz.getSimpleName();
			String expanded = uri + localName;
            
            Node node = NodeFactory.createURI(expanded);
			result = Collections.singletonMap(clazz, node);

    	} else if(rdfType != null ) {
			
//      RdfType rdfType = AnnotationUtils.findAnnotation(beanClass, RdfType.class);
            String iri = rdfType.value();
            if(Strings.isNullOrEmpty(iri)) {
            	iri = "java://" + clazz.getCanonicalName();
            }
            
            String expanded = prefixMapping.expandPrefix(iri);
            Node node = NodeFactory.createURI(expanded);
			result = Collections.singletonMap(clazz, node);
		} else {
			result = Collections.emptyMap();
		}

		return result;
    }

    public static Map<Class<?>, Node> scan(String basePackage, PrefixMapping prefixMapping) {
      Map<Class<?>, Node> result = new HashMap<>();

    	Set<ClassInfo> classInfos;
		try {
			classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(basePackage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for(ClassInfo classInfo : classInfos) {
			Class<?> clazz = classInfo.load();
			
			processClass(clazz, prefixMapping);
		}
		
		return result;
		
    	

//        ClassPathScanningCandidateComponentProvider provider
//            = new ClassPathScanningCandidateComponentProvider(false);
//        provider.addIncludeFilter(new AnnotationTypeFilter(RdfType.class));
//        //return provider;
//        Set<BeanDefinition> beanDefs = provider.findCandidateComponents(basePackage);
//        Map<Class<?>, Node> result = new HashMap<>();
//        for(BeanDefinition beanDef : beanDefs) {
//            String beanClassName = beanDef.getBeanClassName();
//            Class<?> beanClass;
//            try {
//                beanClass = Class.forName(beanClassName); //beanDef.getBeanClassName();
//            } catch(Exception e) {
//                logger.warn("Skipped class due to exception: " + beanClassName);
//                continue;
//            }
//            //Ann
//            RdfType rdfType = AnnotationUtils.findAnnotation(beanClass, RdfType.class);
//            String iri = rdfType.value();
//            String expanded = prologue.getPrefixMapping().expandPrefix(iri);
//            Node node = NodeFactory.createURI(expanded);
//            result.put(beanClass, node);
//        }
//
//        return result;
    }
}
