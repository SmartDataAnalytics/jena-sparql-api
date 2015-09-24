package org.aksw.jena_sparql_api.batch.cli.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.batch.BatchWorkflowManager;
import org.aksw.jena_sparql_api.batch.FunctionFactoryCache;
import org.aksw.jena_sparql_api.batch.FunctionFactoryGeocodeNominatim;
import org.aksw.jena_sparql_api.batch.ListServiceResourceShape;
import org.aksw.jena_sparql_api.batch.QueryTransformConstructGroupedGraph;
import org.aksw.jena_sparql_api.batch.config.ConfigBatchJobDynamic;
import org.aksw.jena_sparql_api.batch.to_review.MapTransformer;
import org.aksw.jena_sparql_api.batch.to_review.MapTransformerSimple;
import org.aksw.jena_sparql_api.beans.json.ContextProcessorJson;
import org.aksw.jena_sparql_api.beans.json.ContextProcessorJsonImpl;
import org.aksw.jena_sparql_api.beans.json.ContextProcessorJsonUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.modifier.Modifier;
import org.aksw.jena_sparql_api.modifier.ModifierDatasetGraphEnrich;
import org.aksw.jena_sparql_api.modifier.ModifierDatasetGraphSparqlUpdate;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeParserJson;
import org.aksw.jena_sparql_api.sparql.ext.json.E_JsonParse;
import org.aksw.jena_sparql_api.sparql.ext.json.E_JsonPath;
import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.AbstractBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;

public class MainBatchWorkflow {

	private static final Logger logger = LoggerFactory.getLogger(MainBatchWorkflow.class);

    public static void main(String[] args) throws Exception {
    	initJenaExtensions();

    	//mainContext(args);
    }

    public static String jsonFn = "http://jsa.aksw.org/fn/json/";

    public static PrefixMapping getDefaultPrefixMapping() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("rdf", RDF.getURI());
        pm.setNsPrefix("rdfs", RDFS.getURI());
        pm.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        pm.setNsPrefix("geom", "http://geovocab.org/geometry#");
        pm.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
        pm.setNsPrefix("fp7o", "http://fp7-pp.publicdata.eu/ontology/");
        pm.setNsPrefix("json", jsonFn);
        pm.setNsPrefix("tmp", "http://example.org/tmp/");
        pm.setNsPrefix("nominatim", "http://jsa.aksw.org/fn/nominatim/");
        pm.setNsPrefix("xsd", XSD.getURI());

        return pm;
    }

    public static void initJenaExtensions() {
        TypeMapper.getInstance().registerDatatype(new RDFDatatypeJson());


        NominatimClient nominatimClient = new JsonNominatimClient(new DefaultHttpClient(), "cstadler@informatik.uni-leipzig.de");
        FunctionRegistry.get().put("http://jsa.aksw.org/fn/nominatim/geocode", FunctionFactoryCache.create(FunctionFactoryGeocodeNominatim.create(nominatimClient)));

        FunctionRegistry.get().put(jsonFn + "parse", E_JsonParse.class);
        FunctionRegistry.get().put(jsonFn + "path", E_JsonPath.class);


        PropertyFunctionRegistry.get().put(jsonFn + "unnest", new PropertyFunctionFactoryJsonUnnest());

        PrefixMapping pm = getDefaultPrefixMapping();

        QueryExecutionFactory qef = FluentQueryExecutionFactory
        	.defaultDatasetGraph()
        	.config()
        		.withPrefixes(pm, true)
        	.end()
        	.create();

        Query query = new Query();
        query.setPrefixMapping(pm);

        QueryFactory.parse(query, "Select * {"
        		+ "  Bind(\"['foo', ['bar', 'baz']]\"^^xsd:json As ?json)\n"
        		+ "  ?json json:unnest ?lvl1.\n"
        		+ "  Optional { ?lvl1 json:unnest ?lvl2. }\n"
        		+ "}", "http://example.org/base/", Syntax.syntaxARQ);

        Prologue prologue = new Prologue(pm);

        QueryExecution qe = qef.createQueryExecution(query);
        System.out.println(ResultSetFormatter.asText(qe.execSelect(), prologue));

    }


    public static void mainContext(String[] args) throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ConfigBatchJobDynamic.class);
        //GenericApplicationContext context = new GenericApplicationContext(foo);


        //context.refresh();

        ConversionService conversionService = context.getBean(ConversionService.class);

        System.out.println(conversionService);
        //conversionService.addConverter(new C_SparqlServiceToQueryExecutionFactory());

        //context.regi

    	ContextProcessorJson contextProcessor = new ContextProcessorJsonImpl(context);

        JsonElement json = readJsonElementFromResource("workflow.js");
        System.out.println(json);
        contextProcessor.processContext(json);

        //CustomP



    	//GenericApplicationContext c = new GenericApplicationContext();
    	//c.get



        //context.register
        //BeanDefinitionRegistry bdr;
        //bdr.regi



        //DefaultListableBeanFactory dlbf;
        //dlbf.

        //context.refresh();

        AbstractBatchConfiguration batchConfig = context.getBean(AbstractBatchConfiguration.class);
        StepBuilderFactory stepBuilders = batchConfig.stepBuilders();

        System.out.println(stepBuilders);
    }


    public static void main1(String[] args) throws Exception {
    	String str = "      Prefix o: <http://fp7-pp.publicdata.eu/ontology/>\n" +
    			"			Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
    			"	            Construct where {\n" +
    			"	              ?s\n" +
    			"	                o:funding [\n" +
    			"	                  o:partner [\n" +
    			"	                    o:address [\n" +
    			"	                      o:country [ rdfs:label ?col ] ;\n" +
    			"	                      o:city [ rdfs:label ?cil ]\n" +
    			"	                    ]\n" +
    			"	                  ]\n" +
    			"	                ]\n" +
    			"	            }\n" +
    			"";


    	Query q = QueryFactory.create(str, Syntax.syntaxSPARQL_11);
    	MappedConcept<DatasetGraph> mc = QueryTransformConstructGroupedGraph.query2(q, Vars.s);
        QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://fp7-pp.publicdata.eu/sparql", "http://fp7-pp.publicdata.eu/").create();

        System.out.println(mc);
    	LookupService<Node, DatasetGraph> ls = LookupServiceUtils.createLookupService(qef, mc);
    	Map<Node, DatasetGraph> map = ls.apply(Arrays.<Node>asList(NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/231648"),  NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/231549")));
    	//ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, false);
    	//Map<Node, Graph> map = ls.fetchData(null, 10l, 0l);


        for(Entry<Node, DatasetGraph> entry : map.entrySet()) {


            System.out.println("=====================================");
            System.out.println(entry.getKey());

            DatasetGraphUtils.write(System.out, entry.getValue());
            //entry.getValue().write(System.out, "N-TRIPLES");
            //Model m = ModelFactory.createModelForGraph(entry.getValue());
            //m.write(System.out, "TURTLE");
        }

    	//main3(args);
    }

    public static void main3(String[] args) throws Exception {

    	Map<String, MapTransformer> keyToTransformer = new HashMap<String, MapTransformer>();
    	keyToTransformer.put("$concept", new MapTransformerSimple());


    	PrefixMapping pm = getDefaultPrefixMapping();


        String testx = "Prefix ex: <http://example.org/> Insert { ?s ex:osmId ?o ; ex:o ?oet ; ex:i ?oei } Where { ?s ex:locationString ?l . Bind(nominatim:geocode(?l) As ?x) . Bind(str(json:path(?x, '$[0].osm_type')) As ?oet) . Bind(str(json:path(?x, '$[0].osm_id')) As ?oei) . Bind(uri(concat('http://linkedgeodata.org/triplify/', ?oet, ?oei)) As ?o) }";


        UpdateRequest test = new UpdateRequest();
        test.setPrefixMapping(pm);
        UpdateFactory.parse(test, testx);


//        ResourceShapeBuilder b = new ResourceShapeBuilder(pm);
//        //b.outgoing("rdfs:label");
//        b.outgoing("geo:lat");
//        b.outgoing("geo:long");
//        b.outgoing("geo:geometry");
//        b.outgoing("geom:geometry").outgoing("ogc:asWKT");

        //b.outgoing("rdf:type").outgoing(NodeValue.TRUE).incoming(ExprUtils.parse("?p = rdfs:label && langMatches(lang(?o), 'en')", pm));

        //ElementTriplesBlock
        //com.hp.hpl.jena.sparql.syntax.

        //Concept concept = Concept.parse("?s | ?s a <http://linkedgeodata.org/ontology/Castle>");
        //Concept concept = Concept.parse("?s | Filter(?s = <http://linkedgeodata.org/triplify/node289523439> || ?s = <http://linkedgeodata.org/triplify/node290076702>)");
        /*
        shape: {
            'fp7o:funding': {
              'fp7o:partner': {
                'fp7o:address': {
                  'fp7o:country': 'rdfs:label',
                  'fp7o:city': 'rdfs:label'
                }
              }
            }


issue: this query syntax will allocate blank nodes :(
yet, we can map them to variables, convert the query to a select form, and group by one of the variables
we can then use an automaton representation and minimize the states, and convert it back to a sparql query
			Prefix o: <http://fp7-pp.publicdata.eu/ontology/>
			Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            Construct {
              ?s
                o:funding [
                  o:partner [
                    o:address [
                      o:country [ rdfs:label ?col ] ;
                      o:city [ rdfs:label ?cil ]
                    ]
                  ]
                ]
            }
            */

        ResourceShapeParserJson parser = new ResourceShapeParserJson(pm);
        Map<String, Object> json = readJsonResource("workflow.js");

        String str = (String)json.get("locationString");
        Modifier<DatasetGraph> m = new ModifierDatasetGraphSparqlUpdate(str);

        ResourceShape rs = parser.parse(json.get("shape"));

        ResourceShape lgdShape = parser.parse(json.get("lgdShape"));

        System.out.println(lgdShape);

        Concept concept = Concept.parse("?s | Filter(?s = <http://fp7-pp.publicdata.eu/resource/project/257943> || ?s = <http://fp7-pp.publicdata.eu/resource/project/256975>)");


        //Query query = ResourceShape.createQuery(rs, concept);
        MappedConcept<DatasetGraph> mappedConcept = ResourceShape.createMappedConcept2(rs, concept);
        System.out.println(mappedConcept);
        MappedConcept<DatasetGraph> mcLgdShape = ResourceShape.createMappedConcept2(lgdShape, null);

        //LookupServiceTransformKey.create(LookupServiceTransformValue.create(base, fn), keyMapper)
        //LookupServiceListService

	        QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://fp7-pp.publicdata.eu/sparql", "http://fp7-pp.publicdata.eu/").create();

        QueryExecutionFactory qefLgd = FluentQueryExecutionFactory.http("http://linkedgeodata.org/sparql", "http://linkedgeodata.org").create();

        //tmp:enrich

        String osmIdToLgd = "Insert { ?s tmp:enrich ?o } Where { ?s tmp:osmId ?id ; tmp:osmEntityType ?et. Bind(concat('http://linkedgeodata.org/triplify/', ?et, ?et) As ?x) }";
        String enrichToSameAs = "Modify Insert { ?s owl:sameAs ?o } Delete { ?s tmp:enrich ?o } Where { ?s tmp:enrich ?o }";
        String fuse1 = "Modify Insert { ?s ?p ?o } Delete { ?x ?p ?o } Where { ?x tmp:fuse ?s ; ?s ?p ?o }";
        //String fuse2 = "Delete { ?s ?p ?o }"


        //LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mappedConcept);

        LookupService<Node, DatasetGraph> lsLgdX = LookupServiceListService.create(ListServiceResourceShape.create(qefLgd, lgdShape));

        //LookupService<Node, Graph> lsLgdX = LookupServiceUtils.createLookupService(qefLgd, mcLgdShape);
        //LookupService<Node, Model> lsLgd2 = LookupServiceTransformValue.create(lsLgdX, F_GraphToModel.fn);
        //LookupService<Resource, Model> lsLgd = LookupServiceTransformKey2.create(lsLgd2, F_ResourceToNode.fn, F_NodeModelToResource.<Resource>create());

        //ListServiceUtils.

        //LookupServiceTransformValue.create(lsLgd, );

        Concept enrich = Concept.parse("?id | ?s ex:osmId ?id", pm);
        Modifier<DatasetGraph> lgdEnrich = new ModifierDatasetGraphEnrich(lsLgdX, enrich);



        ListService<Concept, Node, DatasetGraph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mappedConcept, true);

        Map<Node, DatasetGraph> nodeToDatasetGraph = ls.fetchData(concept, null, null);


        //Map<Resource, Model> resToModel = new LinkedHashMap<Resource, Model>();
        //Map<Resource, Model> resToModel = FN_ToModel.transform(new LinkedHashMap<Resource, Model>(), nodeToGraph, FN_ToModel.fn);
        //resToModel.entrySet().addAll(Collections2.transform(nodeToGraph.entrySet(), FN_ToModel.fn));

        Modifier<DatasetGraph> modi = new ModifierDatasetGraphSparqlUpdate(test);

        for(Entry<Node, DatasetGraph> entry : nodeToDatasetGraph.entrySet()) {

            m.apply(entry.getValue());
            modi.apply(entry.getValue());
            lgdEnrich.apply(entry.getValue());

            System.out.println("=====================================");
            System.out.println(entry.getKey());
            //entry.getValue().write(System.out, "N-TRIPLES");

            DatasetGraphUtils.write(System.out, entry.getValue());
        }
        //System.out.println(nodeToGraph);


        //QueryExecution qe = qef.createQueryExecution(query);
        //Model model = qe.execConstruct();
        //ResultSet resultSet = qe.execSelect();

        //model.write(System.out, "TURTLE");
        //System.out.println(ResultSetFormatter.asText(resultSet));



//        List<Concept> concepts = ResourceShape.collectConcepts(b.getResourceShape());
//        for(Concept concept : concepts) {
//            System.out.println(concept);
//        }
    }


    public static <T> T readJsonResource(String r) throws IOException {
        String str = readResource(r);
        T result = readJson(str);
        return result;
    }

    public static String readResource(String r) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource resource = resolver.getResource(r);
        InputStream in = resource.getInputStream();
        String result = StreamUtils.toString(in);
        return result;
    }

    public static <T> T readJson(String str) throws IOException {
        Gson gson = new Gson();

        //String str = readResource(r);
        Reader reader = new StringReader(str); //new InputStreamReader(in);

        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        Object tmp = gson.fromJson(jsonReader, Object.class);

        @SuppressWarnings("unchecked")
        T result = (T)tmp;
        return result;
    }

    public static JsonElement readJsonElementFromResource(String r) throws IOException {
        String str = readResource(r);
        JsonElement result = readJsonElement(str);
        return result;
    }


    public static JsonElement readJsonElement(String str) throws IOException {
        Gson gson = new Gson();

        Reader reader = new StringReader(str); //new InputStreamReader(in);
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        JsonElement result = gson.fromJson(jsonReader, JsonElement.class);

        return result;
    }


    /**
     * @param args
     * @throws JobParametersInvalidException
     * @throws JobInstanceAlreadyCompleteException
     * @throws JobRestartException
     * @throws JobExecutionAlreadyRunningException
     */
    public static void main2(String[] args) throws Exception
    {


        Map<String, String> classAliasMap = new HashMap<String, String>();
        classAliasMap.put("QueryExecutionFactoryHttp", QueryExecutionFactoryHttp.class.getCanonicalName());

        String str = readResource("workflow.json");
        Map<String, Object> data = readJson(str);
        System.out.println(data);


        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        ContextProcessorJsonUtils.processContext(context, ((Map)data.get("job")).get("context"), classAliasMap);

        context.refresh();

        //System.exit(0);


        //Gson gson = (new GsonBuilder()).

        //cleanUp();
        System.out.println("Test");

        BatchWorkflowManager workflowManager = BatchWorkflowManager.createTestInstance();


        JobExecution je = workflowManager.launchWorkflowJob(str);


        if(je.getStatus().equals(BatchStatus.COMPLETED)) {
//            ResultSet rs = ResultSetFactory.fromXML(new FileInputStream(fileName));
//            while(rs.hasNext()) {
//                System.out.println(rs.nextBinding());
//            }
        }

        //JobExecution je = launchSparqlExport("http://linkedgeodata.org/sparql", Arrays.asList("http://linkedgeodata.org"), "Select * { ?s a <http://linkedgeodata.org/ontology/Airport> }", "/tmp/lgd-airport-uris.txt");

        for(;;) {
            Collection<StepExecution> stepExecutions = je.getStepExecutions();

            for(StepExecution stepExecution : stepExecutions) {
                ExecutionContext sec = stepExecution.getExecutionContext();
                //long processedItemCount = sec.getLong("FlatFileItemWriter.current.count");
                System.out.println("CONTEXT");
                System.out.println(sec.entrySet());
                Thread.sleep(5000);
                //System.out.println(processedItemCount);
            }


            //Set<Entry<String, Object>> entrySet = je.getExecutionContext().entrySet();
            //ExecutionContext ec = je.getExecutionContext();
            //ec.
            //System.out.println(entrySet);
        }


        //ed.shutdown();
    }


}
