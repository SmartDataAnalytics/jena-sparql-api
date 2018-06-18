package org.aksw.jena_sparql_api.batch.cli.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.aksw.commons.util.StreamUtils;
import org.aksw.gson.utils.JsonProcessorKey;
import org.aksw.gson.utils.JsonTransformerRewrite;
import org.aksw.gson.utils.JsonVisitorRewrite;
import org.aksw.gson.utils.JsonWalker;
import org.aksw.jena_sparql_api.batch.BatchWorkflowManager;
import org.aksw.jena_sparql_api.batch.JenaExtensionBatch;
import org.aksw.jena_sparql_api.batch.QueryTransformConstructGroupedGraph;
import org.aksw.jena_sparql_api.batch.config.ConfigBatchJobDynamic;
import org.aksw.jena_sparql_api.batch.config.ConfigParsersCore;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteBeanClassName;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteBeanDefinition;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteClass;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteHop;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteJson;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewritePrefixes;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteShape;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteSimpleJob;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteSparqlFile;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteSparqlPipe;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteSparqlService;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteSparqlStep;
import org.aksw.jena_sparql_api.batch.json.rewriters.JsonVisitorRewriteSparqlUpdate;
import org.aksw.jena_sparql_api.batch.step.FactoryBeanStepLog;
import org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlCount;
import org.aksw.jena_sparql_api.batch.to_review.MapTransformer;
import org.aksw.jena_sparql_api.batch.to_review.MapTransformerSimple;
import org.aksw.jena_sparql_api.beans.json.JsonProcessorContext;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.modifier.Modifier;
import org.aksw.jena_sparql_api.modifier.ModifierDatasetGraphEnrich;
import org.aksw.jena_sparql_api.modifier.ModifierDatasetGraphSparqlUpdate;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeParserJsonObject;
import org.aksw.jena_sparql_api.shape.lookup.MapServiceResourceShapeDataset;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.spring.json.ContextProcessorJsonUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

public class MainBatchWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(MainBatchWorkflow.class);

    public static void mainXml() throws Exception {
        ClassPathXmlApplicationContext test = new ClassPathXmlApplicationContext("testList.xml");
        //org.springframework.beans.factory.xml.BeanDefinitionParser
        Collection<String> allBeans = Arrays.asList(test.getBeanDefinitionNames());
        System.out.println("Got " + allBeans.size() + " beans: " + allBeans);
    }

    public static void foo() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        // JavaScript code in a String
        String script1 = (String)"function hello(name) {print ('Hello, ' + name);}";
        // evaluate script
        engine.eval(script1);

        Invocable inv = (Invocable) engine;

        inv.invokeFunction("hello", "Scripting!!" );  //This one works.
    }


    public static void main(String[] args) throws Exception {
        String str = readResource("job-fetch-data-4-threads.js");

        Reader reader = new StringReader(str); //new InputStreamReader(in);
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonElement o = gson.fromJson(jsonReader, JsonElement.class);
        String canon = gson.toJson(o);


        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        Logger scriptLogger = LoggerFactory.getLogger(MainBatchWorkflow.class.getName() + "-ScriptEngine");
        engine.put("logger", logger);
        ScriptContext ctx  = engine.getContext();
        //Bindings bindings = ctx.getBindings(ScriptContext.GLOBAL_SCOPE);

        engine.eval(readResource("js/lib/lodash/4.3.0/lodash.js"));//, bindings);
        engine.eval(readResource("js/src/rewrite-master.js"));//, bindings);

        List<String> rewriterResourceNames = Arrays.asList(
            "js/src/rewriters/RewriterSparqlHop.js",
            "js/src/rewriters/RewriterJson.js",
            "js/src/rewriters/RewriterPrefixes.js",
            "js/src/rewriters/RewriterSparqlFile.js",
            "js/src/rewriters/RewriterSparqlCount.js",
            "js/src/rewriters/RewriterSparqlService.js",
            "js/src/rewriters/RewriterSparqlStep.js",
            "js/src/rewriters/RewriterSparqlUpdate.js",
            "js/src/rewriters/RewriterBeanClassName.js",
            "js/src/rewriters/RewriterBeanDefinition.js",
            "js/src/rewriters/RewriterSparqlPipe.js",
            "js/src/rewriters/RewriterSimpleJob.js",
            "js/src/rewriters/RewriterShell.js",
            "js/src/rewriters/RewriterLog.js"
        );
        String base = "src/main/resources/";
        for(String name : rewriterResourceNames) {
            engine.eval("load('" + base + "/" + name + "')");
            //engine.eval(readResource(name));
        }


//        new JsonVisitorRewriteSparqlService(),
//        new JsonVisitorRewriteShape(),
//        new JsonVisitorRewriteJson(),
//        new JsonVisitorRewriteSparqlStep(),
//        new JsonVisitorRewriteSimpleJob(),
//        new JsonVisitorRewriteSparqlFile(),
//        new JsonVisitorRewriteSparqlPipe(),
//        new JsonVisitorRewriteSparqlUpdate(),
//        new JsonVisitorRewritePrefixes(),
//        new JsonVisitorRewriteHop(),
//        new JsonVisitorRewriteClass("$dataSource", DriverManagerDataSource.class.getName()),
//        new JsonVisitorRewriteClass("$log", FactoryBeanStepLog.class.getName()),
//        new JsonVisitorRewriteClass("$sparqlCount", FactoryBeanStepSparqlCount.class.getName()),
//        new JsonVisitorRewriteBeanClassName(),
//        new JsonVisitorRewriteBeanDefinition()

        Invocable inv = (Invocable)engine;
        Object tmpJsonStr = inv.invokeFunction("performRewrite", canon);
        String jsonStr = (String)tmpJsonStr;
        String prettyJsonStr = gson.toJson(gson.fromJson(jsonStr, Object.class));

        System.out.println("RESULT\n--------------------------------------------");
        System.out.println(prettyJsonStr);
        //engine.eval("var foo = JSON.parse(data); print(_(Object.keys(foo)).map(function(x) { return 'yay' + x; }));");

        if(true) {
            System.exit(0);
        }

//        String queryStr = "INSERT { ?s tmp:location ?l } WHERE { ?s o:address [ o:country [ rdfs:label ?col ] ; o:city [ rdfs:label ?cil ]  ] BIND(concat(?cil, ' ', ?col) As ?l) }";
//
//        UpdateRequest ur = new UpdateRequest();
//        ur.setPrefixMapping(getDefaultPrefixMapping());
//        UpdateFactory.parse(ur, queryStr, "http://example.org/", Syntax.syntaxARQ);
//        UpdateRequestUtils.fixVarNames(ur);
//        //QueryTransformConstructGroupedGraph.fixVarNames(query);
//        String x = ur.toString();
//
//        System.out.println(x);
//        System.exit(0);



//
//        //QueryExecutionFactoryResource qef = new QueryExecutionFactoryResource("dbpedia-airport-eu-snippet.nt");
//        Graph graph = new GraphResource("dbpedia-airport-eu-snippet.nt");
//        //Graph graph = new GraphResource("/home/raven/tmp/2014-09-09-AerialwayThing.way.sorted.nt");
//        QueryExecutionFactory qef = new QueryExecutionFactoryModel(graph);
//        // ?s a <http://schema.org/Airport> .
//
//        QueryExecution qe = qef.createQueryExecution("Construct { ?s ?p ?o } { ?s ?p ?o }");
//        Iterator<Triple> it = qe.execConstructTriples();
//        while(it.hasNext()) {
//            System.out.println(it.next());
//        }
//        //model.write(System.out, "TTL");
//        System.exit(0);
//
//
//        Prologue p = new Prologue(getDefaultPrefixMapping());
//        SparqlParserConfig c = SparqlParserConfig.create(Syntax.syntaxARQ, p);
//        SparqlStmtParser parser = SparqlStmtParserImpl.create(c);
//
//        SparqlStmt stmt = parser.apply("INSERT DATA { <s> <p> <o> }");
//        System.out.println("Statement: " + stmt);
//

//    	mainXml();
        ApplicationContext baseContext = initBaseContext(null);

        JenaExtensionBatch.initJenaExtensions(baseContext);
        mainContext(baseContext);
    }

    public static String jsonFn = "http://jsa.aksw.org/fn/json/";
    public static String httpFn = "http://jsa.aksw.org/fn/http/";
    public static String termFn = "http://jsa.aksw.org/fn/term/";
    public static String tmpNs = "http://jsa.aksw.org/tmp/";

    public static PrefixMapping getDefaultPrefixMapping() {
        PrefixMapping result = new PrefixMappingImpl();
        addDefaultPrefixMapping(result);
        return result;
    }

    public static void addDefaultPrefixMapping(PrefixMapping pm) {
        pm.setNsPrefix("rdf", RDF.getURI());
        pm.setNsPrefix("rdfs", RDFS.getURI());
        pm.setNsPrefix("owl", OWL.getURI());
        //pm.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        pm.setNsPrefix("geom", "http://geovocab.org/geometry#");
        pm.setNsPrefix("ogc", "http://www.opengis.net/ont/geosparql#");
        //pm.setNsPrefix("o", "http://fp7-pp.publicdata.eu/ontology/");
        pm.setNsPrefix("nominatim", "http://jsa.aksw.org/fn/nominatim/");
        pm.setNsPrefix("xsd", XSD.getURI());
        pm.setNsPrefix("json", jsonFn);
        pm.setNsPrefix("http", httpFn);
        pm.setNsPrefix("term", termFn);
        pm.setNsPrefix("tmp", tmpNs);

    }

    public static ApplicationContext initBaseContext(ApplicationContext appContext) {
        AnnotationConfigApplicationContext coreContext = new AnnotationConfigApplicationContext();
        //if(appContext != null) {
            coreContext.setParent(appContext);
        //}
        coreContext.register(ConfigParsersCore.class);
        coreContext.refresh();


        AnnotationConfigApplicationContext baseContext = new AnnotationConfigApplicationContext();
        baseContext.setParent(coreContext);
        baseContext.register(ConfigBatchJobDynamic.class);
        baseContext.refresh();

        return baseContext;
    }

    public static void copyScopes(GenericApplicationContext targetCtx, GenericApplicationContext sourceCtx) {
        for(String scopeName : sourceCtx.getBeanFactory().getRegisteredScopeNames()) {
            Scope scope = sourceCtx.getBeanFactory().getRegisteredScope(scopeName);
            targetCtx.getBeanFactory().registerScope(scopeName, scope);
        }
    }

    public static AnnotationConfigApplicationContext initBatchContext(ApplicationContext baseContext) {
        //ApplicationContext baseContext = initBaseContext();
        //AnnotationConfigApplicationContext baseContext = new AnnotationConfigApplicationContext(ConfigBatchJobDynamic.class);
        //AnnotationConfigApplicationContext x = new AnnotationConfigApplicationContext()


//        GenericApplicationContext batchContext = new GenericApplicationContext(baseContext);
//        AnnotationConfigUtils.registerAnnotationConfigProcessors(batchContext);


        AnnotationConfigApplicationContext batchContext = new AnnotationConfigApplicationContext();
        batchContext.setParent(baseContext);

        copyScopes(batchContext, (GenericApplicationContext)baseContext);

        return batchContext;
    }

    /**
     * Returns a NON-REFRESHED context object
     * This means that modifications on the returned object are still allowed,
     * however .refresh() must be eventually called
     *
     *
     * @param baseContext
     * @param json
     * @return
     * @throws Exception
     */
    public static AnnotationConfigApplicationContext initContext(ApplicationContext baseContext, JsonElement json) throws Exception {

//        Object stepScope = baseContext.getBean("step");
//        System.out.println("StepScope: " + stepScope);

        AnnotationConfigApplicationContext result = initBatchContext(baseContext);

        // Preprocessors
        JsonProcessorKey contextPreprocessor = new JsonProcessorKey();

        // Alias mappings
        // replaces certain type-values with qualified java class names
        //JsonProcessorMap

        // Attribute-based typing (duck typing)
        // src: { serviceUrl: 'http://...' } -> inject type: 'SparqlService'

        // Special type preprocessing:
        // src: $sparqlService: { serviceUrl} -> { type: 'sparqlService',

        //contextPreprocessor.getProcessors().add(alias);

        // Core Beans processor
        JsonProcessorContext contextProcessor = new JsonProcessorContext(result);

        //JsonProcessorMap jobProcessor = new JsonProcessorMap();
        //jobProcessor.register("context", false, contextProcessor);




        //JsonElement jobParamsJson = readJsonElementFromResource("params.js");
        //List<JsonVisitorRewrite> jr = Arrays.<JsonVisitorRewrite>asList(

        JsonVisitorRewriteJobParameters subVisitor = new JsonVisitorRewriteJobParameters();
        JsonVisitorRewriteKeys visitor = JsonVisitorRewriteKeys.create(JsonTransformerRewrite.create(subVisitor, false));

        //JsonElement effectiveJobParamsJson = JsonWalker.visit(jobParamsJson, visitor);

        //UserDefinedFunctionFactory fff;
        // my:foo(?x, ?y ?z) = someotherexpression
        // by convention, the expression must be of type E_Equals with the left hand side must be a function with only variables as arguments
        // we could add a hack to allow := instead (well, in addition to) =

        //);

        //JsonElement effectiveJobParamsJson = JsonWalker.rewrite(jobParamsJson, jr);

        //System.out.println("Job params: " +  new GsonBuilder().setPrettyPrinting().create().toJson(effectiveJobParamsJson));

        //JobParameters jobParams = JobParametersJsonUtils.toJobParameters(effectiveJobParamsJson, null);

        //System.out.println(jobParams);


        //System.exit(0);
        //GenericBeanDefinition x;
        //x.

        //List<JsonVisitorRewrite> rewriters = Collections.emptyList();
//        List<JsonVisitorRewrite> rewriters = Arrays.<JsonVisitorRewrite>asList(
//                new JsonVisitorRewriteSparqlService(),
//                new JsonVisitorRewriteShape(),
//                new JsonVisitorRewriteJson(),
//                new JsonVisitorRewriteSparqlStep(),
//                new JsonVisitorRewriteSimpleJob(),
//                new JsonVisitorRewriteSparqlFile(),
//                new JsonVisitorRewriteSparqlPipe(),
//                new JsonVisitorRewriteSparqlUpdate(),
//                new JsonVisitorRewritePrefixes(),
//                new JsonVisitorRewriteHop()
//        );
//        json = JsonWalker.rewriteUntilNoChange(json, rewriters);
        json = rewrite(json);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String str = gson.toJson(json);
        logger.debug("Final JSON specification: " + str);
        //System.out.println(str);

        contextProcessor.process(json);



        //contextProcessor.process(json);


        //batchContext.refresh();

        return result;
    }

    public static BeanDefinition beanDefinitionOfType(BeanDefinitionRegistry context, Class<?> clazz) {
        BeanDefinition result = null;

        //jobProcessor.process(json);
        String[] names = context.getBeanDefinitionNames();
        for(String name : names) {
            BeanDefinition bd = context.getBeanDefinition(name);
            String className = bd.getBeanClassName();
            Class<?> c;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException e) {
                logger.warn("Unexpected non-fatal expection", e);
                continue;
                // Nothing to do here; we just ignore it
            }

            // If the clazz is a FactoryBean, get its type
            if(FactoryBean.class.isAssignableFrom(c)) {
                FactoryBean<?> fb;
                try {
                    fb = (FactoryBean<?>)c.newInstance();
                } catch (Exception e) {
                    logger.warn("Unexpected non-fatal exception", e);
                    continue;
                }
                c = fb.getObjectType();
                // Just skip these kind of factories - e.g. ScopedProxyFactoryBean does not yield an object type
//                if(c == null) {
//                    throw new RuntimeException("Bean factory that does not declare an object type encountered: " + fb.getClass() + " - " + fb);
//                }
            }

            if(c != null && clazz.isAssignableFrom(c)) {
                result = bd;
                break;
            }
            //BeanFactoryUtils.beanOfType(batchContext, Job.class);
            //BeanFactoryUtils.
        }

        return result;
    }

    public static JsonElement rewrite(JsonElement json) throws Exception{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String jsonStr = gson.toJson(json);
//        JsonElement o = gson.fromJson(jsonReader, JsonElement.class);
//        String canon = gson.toJson(o);


        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        Logger scriptLogger = LoggerFactory.getLogger(MainBatchWorkflow.class.getName() + "-ScriptEngine");
        engine.put("logger", logger);
        ScriptContext ctx  = engine.getContext();
        //Bindings bindings = ctx.getBindings(ScriptContext.GLOBAL_SCOPE);

        engine.eval(readResource("js/lib/lodash/4.3.0/lodash.js"));//, bindings);
        engine.eval(readResource("js/src/rewrite-master.js"));//, bindings);

        List<String> rewriterResourceNames = Arrays.asList(
            "js/src/rewriters/RewriterJson.js",
            "js/src/rewriters/RewriterPrefixes.js",
            "js/src/rewriters/RewriterSparqlFile.js",
            "js/src/rewriters/RewriterSparqlCount.js",
            "js/src/rewriters/RewriterSparqlService.js",
            "js/src/rewriters/RewriterSparqlStep.js",
            "js/src/rewriters/RewriterSparqlHop.js",
            "js/src/rewriters/RewriterSparqlUpdate.js",
            "js/src/rewriters/RewriterBeanClassName.js",
            "js/src/rewriters/RewriterBeanDefinition.js",
            "js/src/rewriters/RewriterSparqlPipe.js",
            "js/src/rewriters/RewriterLog.js",
            "js/src/rewriters/RewriterShell.js"
        );
        //String base = "src/main/resources/";
        String base = "/home/raven/Projects/Eclipse/jena-sparql-api-parent/jena-sparql-api-batch/src/main/resources/";
        for(String name : rewriterResourceNames) {
            engine.eval("load('" + base + "/" + name + "')");
            //engine.eval(readResource(name));
        }


//        new JsonVisitorRewriteSparqlService(),
//        new JsonVisitorRewriteShape(),
//        new JsonVisitorRewriteJson(),
//        new JsonVisitorRewriteSparqlStep(),
//        new JsonVisitorRewriteSimpleJob(),
//        new JsonVisitorRewriteSparqlFile(),
//        new JsonVisitorRewriteSparqlPipe(),
//        new JsonVisitorRewriteSparqlUpdate(),
//        new JsonVisitorRewritePrefixes(),
//        new JsonVisitorRewriteHop(),
//        new JsonVisitorRewriteClass("$dataSource", DriverManagerDataSource.class.getName()),
//        new JsonVisitorRewriteClass("$log", FactoryBeanStepLog.class.getName()),
//        new JsonVisitorRewriteClass("$sparqlCount", FactoryBeanStepSparqlCount.class.getName()),
//        new JsonVisitorRewriteBeanClassName(),
//        new JsonVisitorRewriteBeanDefinition()

        Invocable inv = (Invocable)engine;
        Object tmpJsonOutStr = inv.invokeFunction("performRewrite", jsonStr);
        String jsonOutStr = (String)tmpJsonOutStr;

        Reader reader = new StringReader(jsonOutStr); //new InputStreamReader(in);
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        JsonElement result = gson.fromJson(jsonReader, JsonElement.class);

        String canon = gson.toJson(result);
        logger.info("Using configuration: " + canon);

        return result;


        //String prettyJsonOutStr = gson.toJson(gson.fromJson(jsonOutStr, Object.class));

        //System.out.println("RESULT\n--------------------------------------------");
        //System.out.println(prettyJsonStr);

    }

    public static JsonElement rewriteOld(JsonElement json) {
        List<JsonVisitorRewrite> rewriters = Arrays.<JsonVisitorRewrite>asList(
                new JsonVisitorRewriteSparqlService(),
                new JsonVisitorRewriteShape(),
                new JsonVisitorRewriteJson(),
                new JsonVisitorRewriteSparqlStep(),
                new JsonVisitorRewriteSimpleJob(),
                new JsonVisitorRewriteSparqlFile(),
                new JsonVisitorRewriteSparqlPipe(),
                new JsonVisitorRewriteSparqlUpdate(),
                new JsonVisitorRewritePrefixes(),
                new JsonVisitorRewriteHop(),
                new JsonVisitorRewriteClass("$dataSource", DriverManagerDataSource.class.getName()),
                new JsonVisitorRewriteClass("$log", FactoryBeanStepLog.class.getName()),
                new JsonVisitorRewriteClass("$sparqlCount", FactoryBeanStepSparqlCount.class.getName()),
                new JsonVisitorRewriteBeanClassName(),
                new JsonVisitorRewriteBeanDefinition()
        );
        JsonElement result = JsonWalker.rewriteUntilNoChange(json, rewriters);
        return result;
    }

    public static void mainContext(ApplicationContext baseContext) throws Exception {
        JsonElement jobJson = readJsonElementFromResource("workflow.js");

        ApplicationContext batchContext = initContext(baseContext, jobJson);

//        SparqlService test = (SparqlService)batchContext.getBean("sourceFile");
//        System.out.println("SourceFile: " + test);

        JobOperator jobOperator = batchContext.getBean(JobOperator.class);
        //JobLauncher jobLauncher = batchContext.getBean(JobLauncher.class);
        Job job = batchContext.getBean(Job.class);


        System.out.println("STEP: " + ((SimpleJob)job).getStepNames());


        Collection<String> allBeans = Arrays.asList(batchContext.getBeanDefinitionNames());
        System.out.println("Got " + allBeans.size() + " beans: " + allBeans);

        System.out.println("Job: " + job);

        //System.exit(0);

        BatchWorkflowManager manager = batchContext.getBean(BatchWorkflowManager.class);//new BatchWorkflowManager(config);

        JobExecution exec = manager.launch(job, new JobParameters());


        Thread.sleep(3000);
        System.out.println("Waited for 3 sec");

        for(Throwable t : exec.getAllFailureExceptions()) {
            t.printStackTrace();
        }
//        Object foo = batchContext.getBean("steps");
//        System.out.println(foo);

        //System.out.println(jobOperator);
        //jobOperator.st
        //jobOperator.start(job.getName(), );
        //jobLauncher.

        //BeanDefinition x;
        //x.get

        //AbstractBatchConfiguration batchConfig = context.getBean(AbstractBatchConfiguration.class);
        //SimpleBatchConfiguration x;
        //batchConfig.job


        //StepBuilderFactory stepBuilders = batchConfig.stepBuilders();

        //System.out.println(stepBuilders);
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
        Map<Node, DatasetGraph> map = ls.fetchMap(Arrays.<Node>asList(NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/231648"),  NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/231549")));
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

    public static void write(PrintStream out, Map<Node, DatasetGraph> map) {
        for(Entry<Node, DatasetGraph> entry : map.entrySet()) {


            System.out.println("=====================================");
            System.out.println(entry.getKey());

            DatasetGraphUtils.write(out, entry.getValue());
            //entry.getValue().write(System.out, "N-TRIPLES");
            //Model m = ModelFactory.createModelForGraph(entry.getValue());
            //m.write(System.out, "TURTLE");
        }

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
        //org.apache.jena.sparql.syntax.

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
//BeanDefinitionHolder x = new
        //BeanDefinitionReaderUtils.


        ResourceShapeParserJsonObject parser = new ResourceShapeParserJsonObject(pm);
        Map<String, Object> json = readJsonResource("workflow.js");

        String str = (String)json.get("locationString");
        Modifier<DatasetGraph> m = new ModifierDatasetGraphSparqlUpdate(str);

        ResourceShape rs = parser.parse(json.get("shape"));

        ResourceShape lgdShape = parser.parse(json.get("lgdShape"));

        System.out.println(lgdShape);

        Concept concept = Concept.parse("?s | Filter(?s = <http://fp7-pp.publicdata.eu/resource/project/257943> || ?s = <http://fp7-pp.publicdata.eu/resource/project/256975>)");


        //Query query = ResourceShape.createQuery(rs, concept);
        MappedConcept<DatasetGraph> mappedConcept = ResourceShape.createMappedConcept2(rs, concept, false);
        System.out.println(mappedConcept);
        MappedConcept<DatasetGraph> mcLgdShape = ResourceShape.createMappedConcept2(lgdShape, null, false);

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

        LookupService<Node, DatasetGraph> lsLgdX = LookupServiceListService.create(MapServiceResourceShapeDataset.create(qefLgd, lgdShape, true));

        //LookupService<Node, Graph> lsLgdX = LookupServiceUtils.createLookupService(qefLgd, mcLgdShape);
        //LookupService<Node, Model> lsLgd2 = LookupServiceTransformValue.create(lsLgdX, F_GraphToModel.fn);
        //LookupService<Resource, Model> lsLgd = LookupServiceTransformKey2.create(lsLgd2, F_ResourceToNode.fn, F_NodeModelToResource.<Resource>create());

        //ListServiceUtils.

        //LookupServiceTransformValue.create(lsLgd, );

        Concept enrich = Concept.parse("?id | ?s ex:osmId ?id", pm);
        Modifier<DatasetGraph> lgdEnrich = new ModifierDatasetGraphEnrich(lsLgdX, enrich);



        MapService<Concept, Node, DatasetGraph> ls = MapServiceUtils.createListServiceMappedConcept(qef, mappedConcept, true);

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


    public static String readResource(String r) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource resource = resolver.getResource(r);
        InputStream in = resource.getInputStream();
        String result = StreamUtils.toString(in);
        return result;
    }

    public static JsonElement readJsonElementFromResource(String r) throws IOException {
        String str = readResource(r);
        JsonElement result = readJsonElement(str);
        return result;
    }

//    public static JsonElement readJsonElementFromResource(Resource resource) throws IOException {
//
//    }


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


//        JobExecution je = workflowManager.launchWorkflowJob(str);
//
//
//        if(je.getStatus().equals(BatchStatus.COMPLETED)) {
////            ResultSet rs = ResultSetFactory.fromXML(new FileInputStream(fileName));
////            while(rs.hasNext()) {
////                System.out.println(rs.nextBinding());
////            }
//        }

        //JobExecution je = launchSparqlExport("http://linkedgeodata.org/sparql", Arrays.asList("http://linkedgeodata.org"), "Select * { ?s a <http://linkedgeodata.org/ontology/Airport> }", "/tmp/lgd-airport-uris.txt");
//
//        for(;;) {
//            Collection<StepExecution> stepExecutions = je.getStepExecutions();
//
//            for(StepExecution stepExecution : stepExecutions) {
//                ExecutionContext sec = stepExecution.getExecutionContext();
//                //long processedItemCount = sec.getLong("FlatFileItemWriter.current.count");
//                System.out.println("CONTEXT");
//                System.out.println(sec.entrySet());
//                Thread.sleep(5000);
//                //System.out.println(processedItemCount);
//            }
//
//
//            //Set<Entry<String, Object>> entrySet = je.getExecutionContext().entrySet();
//            //ExecutionContext ec = je.getExecutionContext();
//            //ec.
//            //System.out.println(entrySet);
//        }


        //ed.shutdown();
    }


}
