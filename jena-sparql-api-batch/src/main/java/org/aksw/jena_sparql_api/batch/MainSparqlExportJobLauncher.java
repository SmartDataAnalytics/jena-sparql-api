package org.aksw.jena_sparql_api.batch;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;

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

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;



public class MainSparqlExportJobLauncher {

    private static final Logger logger = LoggerFactory.getLogger(MainSparqlExportJobLauncher.class);


    /**
     * @param args
     * @throws JobParametersInvalidException
     * @throws JobInstanceAlreadyCompleteException
     * @throws JobRestartException
     * @throws JobExecutionAlreadyRunningException
     */
    public static void main(String[] args) throws Exception
    {
        //cleanUp();
        System.out.println("Test");
        //launchSparqlExport("http://dbpedia.org/sparql", Arrays.asList("http://dbpedia.org"), "Select * { ?s ?p ?o } Limit 10", "/tmp/foobar.txt");
        //launchSparqlExport("http://fp7-pp.publicdata.eu/sparql", Arrays.asList("http://fp7-pp.publicdata.eu/"), "Select * { ?s ?p ?o }", "/tmp/fp7.txt");



        //JobExecution je = launchSparqlExport("http://linkedgeodata.org/sparql", Arrays.asList("http://linkedgeodata.org"), "Select * { ?s a <http://linkedgeodata.org/ontology/Airport> . ?s ?p ?o }", "/tmp/lgd-airports.txt");
        //JobExecution je = launchSparqlExport("http://localhost:8870/sparql", Arrays.asList("http://demo.geoknow.eu/y1/sparqlify/hotel-reviews/"), "Select * { ?s a <http://purl.org/acco/ns#Hotel> . ?s ?p ?o }", "/tmp/hotel-reviews.txt");
        String fileName = "/tmp/people8.txt";
        //String queryString = "Select * { ?s a <http://schema.org/Person> . }";
        String queryString = "Select * { ?s a <http://schema.org/Airport> . }";

        SparqlExportManager sparqlExportManager = SparqlExportManager.createTestInstance();

        JobExecution je = sparqlExportManager.launchSparqlExport("http://localhost/data/dbpedia/3.9/sparql", Arrays.asList("http://dbpedia.org/3.9/"), queryString, fileName);


        if(je.getStatus().equals(BatchStatus.COMPLETED)) {
            ResultSet rs = ResultSetFactory.fromXML(new FileInputStream(fileName));
            while(rs.hasNext()) {
                System.out.println(rs.nextBinding());
            }
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
    }


}
