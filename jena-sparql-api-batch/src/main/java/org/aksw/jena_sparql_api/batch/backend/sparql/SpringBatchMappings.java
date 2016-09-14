package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.beans.model.EntityModel;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyModel;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.util.BeanUtils;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeDate;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
//import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

public class SpringBatchMappings {

    
	public static <I, O> Function<I, O> memoize(Function<I, O> fn) {
		Map<I, O> cache = new HashMap<>();
		
		Function<I, O> result = (i) -> cache.computeIfAbsent(i, fn);
		return result;
	}
	
    
	public static void test() {
//	    TypeDeciderImpl typeDecider = new TypeDeciderImpl();
	    //typeDecider.addMapping(
	    //typeDecider.exposeShape(rsb);
//	    ResourceShapeBuilder rsb = new ResourceShapeBuilder();
//	    typeDecider.exposeShape(rsb);
//	    ResourceShape rs = rsb.getResourceShape();
//	    ResourceShape.fetchData(qef, rs, NodeFactory.createURI("http://ex.org/11"));
//	    
	    
		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
		bean.afterPropertiesSet();

		ConversionService cs = bean.getObject();

//		cs.convert(source, targetType);

    	Long value = 1l;

    	TypeMapper tm = TypeMapper.getInstance();
    	RDFDatatype dt = tm.getTypeByClass(value.getClass());
    	
    	//Object y = dt.cannonicalise(value);
    	//dt.getJavaClass()
    	
    	
    	String lex = dt.unparse(value);
    	Node node = NodeFactory.createLiteral(lex, dt);
    	Object o = dt.parse(lex);
    	System.out.println(o.getClass());
    	
    	Object x = node.getLiteralValue();
    	System.out.println("Got value: " + x.getClass() + " " + node);
    	
	}

	
	public static void main(String[] args) {    	
		EntityModel.createDefaultModel(Boolean.class, null);
		
		
		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
		bean.afterPropertiesSet();

		ConversionService conversionService = bean.getObject();		
    	
    	
//        ExecutionContext ecx = new ExecutionContext();
//        MapOps o = new MapOpsExecutionContext();
//        o.put(ecx, "test", "hello");
//        System.out.println("execution context size: " + o.size(ecx));
        
        
        
        
        // TODO: For each java class, set a white / blacklist of properties which to process
        //Map<Class<?>, Map<String, String>> defs = new HashMap<>();
        

        // TODO: Add support for suppliers of new instance
        // TODO: Add support to provide a setter for read only property
        // createForcedFieldSetter(Class, fieldName).setValue(obj, foo);
        
        TypeMapper tm = TypeMapper.getInstance();
        
        tm.registerDatatype(new RDFDatatypeDate());
        
        Map<Class<?>, EntityOps> customOps = new HashMap<>();

        
        
        
        {
            Set<String> excludeProperties = new HashSet<>(Arrays.asList("executionContext", "exitStatus", "status"));

            Map<String, String> pmap = BeanUtils.getPropertyNames(new ExecutionContext()).stream()
                    .filter(p -> !excludeProperties.contains(p))
                .collect(Collectors.toMap(e -> e, e -> "http://batch.aksw.org/ontology/" + e));
            
            EntityModel entityModel = EntityModel.createDefaultModel(ExecutionContext.class, conversionService);
            
            entityModel.setAnnotationFinder((clazz) -> {
                if(clazz.equals(DefaultIri.class)) {
                    DefaultIri x = new DefaultIriAnnotation("http://ex.org/#{id}");
                    return x;
                };
                return null;                
            });
            
            for(PropertyModel pm : entityModel.getProperties()) {
                pm.setAnnotationFinder((clazz) -> {
                    if(clazz.equals(Iri.class)) {
                        String str = pmap.get(pm.getName());
                        if(str != null) {
                            Iri x = new IriAnnotation(str);
                            return x;
                        }
                    };
                    return null;
                });
            }
                        
        }
        
        {
            Set<String> excludeProperties = new HashSet<>(Arrays.asList("exitStatus", "status"));
            
            Map<String, String> pmap = BeanUtils.getPropertyNames(new JobExecution(0l)).stream()
                    .filter(p -> !excludeProperties.contains(p))
                .collect(Collectors.toMap(e -> e, e -> "http://batch.aksw.org/ontology/" + e));
            
            EntityModel entityModel = EntityModel.createDefaultModel(JobExecution.class, conversionService);
            entityModel.setNewInstance(() -> new JobExecution(0l));
            
            entityModel.setAnnotationFinder((clazz) -> {
                if(clazz.equals(DefaultIri.class)) {
                    DefaultIri x = new DefaultIriAnnotation("http://ex.org/#{id}");
                    return x;
                };
                return null;                
            });
            
            for(PropertyModel pm : entityModel.getProperties()) {
                pm.setAnnotationFinder((clazz) -> {
                    if(pm.getName().equals("executionContext") && clazz.equals(DefaultIri.class)) {
                        return new DefaultIriAnnotation("http://ex.org/foobar/#{id}");
                    }
                    
                    
                    if(clazz.equals(Iri.class)) {
                        String str = pmap.get(pm.getName());
                        if(str != null) {
                            Iri x = new IriAnnotation(str);
                            return x;
                        }
                    };
                    return null;
                });
            }
            
            
            JobExecution inst = (JobExecution)entityModel.newInstance();
            //inst.
            entityModel.getProperty("id").setValue(inst, 12l);
            customOps.put(JobExecution.class, entityModel);
            
            
            EntityModel ecModel = EntityModel.createDefaultModel(ExecutionContext.class, conversionService);
            ecModel.setCollectionOps(new CollectionOpsExecutionContext());
            customOps.put(ExecutionContext.class, ecModel);
        }
        
        
        FunctionMemoize<Class<?>, EntityOps> classToOps = new FunctionMemoize<>((clazz) -> EntityModel.createDefaultModel(clazz, conversionService));
        classToOps.getCache().putAll(customOps);
        
//        Function<Class<?>, EntityOps> classToOps = (clazz) -> {
//            EntityOps result;
//            
//            EntityOps cops = customOps.computeIfAbsent(clazz);
//            
//            result = cops != null
//                    ? cops
//                    : EntityModel.createDefaultModel(clazz, conversionService);
//            
//            return result;
//        };

        //typeFactory.registerTypeAdator(Class<?>, );
//        GsonBuilder x;
//        TypeAdapterFactory
        
        
        RdfTypeFactoryImpl typeFactory =  RdfTypeFactoryImpl.createDefault(null, classToOps, conversionService);
        
        typeFactory.getClassToRdfType().put(ExecutionContext.class, new RdfTypeMap(MapExecutionContext::createMapView));
        
        
        RdfType t = typeFactory.forJavaType(JobExecution.class);
        //t.emitTriples(persistenceContext, emitterContext, out, obj);

        SparqlService sparqlService = FluentSparqlService.forModel().create();
        
        RdfMapperEngine engine = new RdfMapperEngineImpl(sparqlService, typeFactory);
        //engine.find(clazz, rootNode);
        JobExecution entity = new JobExecution(11l);
        ExecutionContext ec = new ExecutionContext();
        ec.put("hello", "world");
        ec.put("foo", 666);
        
        entity.setExecutionContext(ec);
        
        
        engine.merge(entity);
        //engine.emitTriples(graph, entity);
        
        Model model = sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
        System.out.println("Graph:");
        model.write(System.out, "TTL");
        
        JobExecution lr = engine.find(JobExecution.class, NodeFactory.createURI("http://ex.org/11"));
        System.out.println("Lookup result: " + lr);
        
        //lr.setVersion(111);
        engine.merge(lr);

        
        System.out.println("Graph:");
        sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
        
        System.out.println("Lookup result: " + engine.find(JobExecution.class, NodeFactory.createURI("http://ex.org/11")));

        
        //EntityManagerJena em = new EntityManagerJena(engine)
        
        //Function<Object, String> iriFn = (je) -> ":" + ((JobExecution)je).getJobId();
        //RdfClass map = new RdfClass(typeFactory, JobExecution.class, (je) -> ":" + ((JobExecution)je).getJobId());

        
        //System.out.println(inst);
//        
//        Supplier<JobExecution> newInstance = () -> new JobExecution(0l);
//        Function<Object, String> iriFn = (je) -> ":" + ((JobExecution)je).getJobId();
//        Map<String, String> pmap = BeanUtils.getPropertyNames(o).stream()
//                .collect(Collectors.toMap(e -> e, e -> "http://batch.aksw.org/ontology/" + e));
//        builder.setSetterOverride("id", (val) -> {
//            Field field = JobExecution.class.getDeclaredField("id");
//            field.setAccessible(true);
//        });
//        
        
        //System.out.println(entityModel.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList()));
        
//        //listMethodNames
//        BeanWrapper wrapper;
//        wrapper.getPropertyDescriptors()
//        Map<String, Object> 
//        
//        
//        RdfTypeFactory typeFactory =  RdfTypeFactoryImpl.createDefault();
//        typeFactory.forJavaType(clazz)
//        
//        //Function<JobExecution, String> iriFn = (je) -> ":" + je.getJobId();
//        RdfClass map = new RdfClass(typeFactory, JobExecution.class, (je) -> ":" + ((JobExecution)je).getJobId());
//        
//        RdfPropertyDescriptor
//        map.addPropertyDescriptor(propertyDescriptor);
    }
}
