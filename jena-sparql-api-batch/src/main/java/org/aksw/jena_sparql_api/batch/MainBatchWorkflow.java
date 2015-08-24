package org.aksw.jena_sparql_api.batch;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



public class MainBatchWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(MainBatchWorkflow.class);

    
    public static void main(String[] args) throws Exception {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefix("rdf", RDF.getURI());
        pm.setNsPrefix("rdfs", RDFS.getURI());
        
        ResourceShapeBuilder b = new ResourceShapeBuilder(pm);
        b.outgoing("rdfs:label");
        b.outgoing("rdf:type");
        
        List<Concept> concepts = ResourceShape.collectConcepts(null);
        for(Concept concept : concepts) {
            System.out.println(concept);
        }
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
        
        Gson gson = new Gson();
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("workflow.json");        
        InputStream in = resource.getInputStream();
        String str = StreamUtils.toString(in);
        Reader reader = new StringReader(str); //new InputStreamReader(in);
        
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
        Map<String, Object> data = gson.fromJson(jsonReader, Map.class);
        System.out.println(data);
        
        
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        
        JsonContextProcessor.processContext(context, ((Map)data.get("job")).get("context"), classAliasMap);
        
        context.refresh();
        
        System.exit(0);
        
        
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
