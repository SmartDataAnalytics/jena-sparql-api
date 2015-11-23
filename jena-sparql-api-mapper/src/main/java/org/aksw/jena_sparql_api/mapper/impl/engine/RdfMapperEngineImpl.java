package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContextImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfPropertyDescriptor;
import org.aksw.jena_sparql_api.mapper.model.RdfPopulator;
import org.aksw.jena_sparql_api.mapper.model.RdfPopulatorProperty;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.proxy.MethodInterceptorRdf;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Quad;

public class RdfMapperEngineImpl
	implements RdfMapperEngine
{
    protected Prologue prologue;
    //protected QueryExecutionFactory qef;
    protected SparqlService sparqlService;

    protected RdfTypeFactory typeFactory;
    protected RdfPopulationContext populationContext;

    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory) {
    	this(sparqlService, typeFactory, new RdfPopulationContextImpl());
    }


//QueryExecutionFactory qef
    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory, RdfPopulationContext populationContext) {
        this.sparqlService = sparqlService;
        this.typeFactory = typeFactory;
        this.populationContext = populationContext;
    }


//    public static resolvePopulation(QueryExecutionFactory qef) {
//
//    }

    public <T> LookupService<Node, T> getLookupService(Class<T> clazz) {
    	return null;
    }

    public ListService<Concept, Node, DatasetGraph> prepareListService(RdfClass rdfClass, Concept filterConcept) {
    	Concept classConcept = rdfClass.getConcept();
    	//Concept concept = ConceptUtils.createCombinedConcept(attrConcept, filterConcept, renameVars, attrsOptional, filterAsSubquery)
    	//TODO Create a combined concept
    	Concept concept = filterConcept;


        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);

        //rdfClass.build(builder);
        for(RdfPopulator populator : rdfClass.getPopulators()) {
        	populator.exposeShape(builder);
        }

        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        ResourceShape shape = builder.getResourceShape();
        //MappedConcept<DatasetGraph> mc = ResourceShape.createMappedConcept2(shape, null);
        MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null);
        ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, true);

        //Gragh graph;

        Map<Node, Graph> nodeToGraph = ls.fetchData(null, null, null);

        // For each node...
        //RdfPopulationContext populationContext;
        RdfPopulationContextImpl context = new RdfPopulationContextImpl();
        for(Entry<Node, Graph> entry : nodeToGraph.entrySet()) {
        	Node subject = entry.getKey();
        	Graph graph = entry.getValue();

        	Object bean = context.objectFor(rdfClass, subject);


        	//context
        	//BeanState beanState = context.getBeanState(rdfClass, subject);


        	// Run the class's populators
            for(RdfPopulator populator : rdfClass.getPopulators()) {
            	populator.populateBean(context, bean, graph, subject);
            }

            context.setPopulated(bean, true);

            // Mark the class as populated
            //context.setBeanState(bean, "populated", true);
            //beanState.setPopulated(true);

            // Check with property values need further population
            for(RdfPropertyDescriptor pd : rdfClass.getPropertyDescriptors()) {
            	if(pd.getRdfType().isSimpleType()) {

            	}
            }
        }




        //rdfClass.createJavaObject(subject);




        return null;
    }


//    public MappedConcept<DatasetGraph> getMappedQuery(ResourceShapeBuilder builder, RdfClass rdfClass) {
//
//        Collection<RdfProperty> rdfProperties = rdfClass.getRdfProperties();
//
//        for(RdfProperty rdfProperty : rdfProperties) {
//            processProperty(builder, rdfProperty);
//        }
//
//        ResourceShape shape = builder.getResourceShape();
//        MappedConcept<DatasetGraph> result = ResourceShape.createMappedConcept2(shape, null);
//        return result;
//    }


    public void processProperty(ResourceShapeBuilder builder, RdfPopulatorProperty rdfProperty) {
        //Relation relation = rdfProperty.getRelation();
    	//Node predicate = rdfProperty.get
        //builder.outgoing(relation);

        //rdfProperty.getTargetRdfClass()
    }


    @Override
    public <T> T merge(T entity) {
	    MethodInterceptorRdf interceptor = RdfClass.getMethodInterceptor(entity);

	    DatasetGraph oldState = interceptor == null
	            ? DatasetGraphFactory.createMem()
	            : interceptor.getDatasetGraph()
	            ;

	    Class<?> clazz = entity.getClass();
	    //RdfClass rdfClass = RdfClassFactory.createDefault(prologue).create(clazz);
	    RdfClass rdfClass = (RdfClass)typeFactory.forJavaType(clazz);


	    DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
	    String gStr = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);
	    if(gStr == null) {
	        throw new RuntimeException("No target graph specified");
	    }
	    Node g = NodeFactory.createURI(gStr);

	    DatasetGraph newState = DatasetGraphFactory.createMem();
	    Graph out = newState.getGraph(g);
	    rdfClass.writeGraph(out, entity);

	    System.out.println("oldState");
	    DatasetGraphUtils.write(System.out, oldState);

	    System.out.println("newState");
	    DatasetGraphUtils.write(System.out, newState);

	    Diff<Set<Quad>> diff = UpdateDiffUtils.computeDelta(newState, oldState);
	    System.out.println("diff: " + diff);
	    UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
	    UpdateExecutionUtils.executeUpdate(uef, diff);

	    return entity;
    }


	@Override
	public RdfTypeFactory getRdfTypeFactory() {
		return typeFactory;
	}


}
