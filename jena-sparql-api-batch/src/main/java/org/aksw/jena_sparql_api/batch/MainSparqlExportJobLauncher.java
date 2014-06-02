package org.aksw.jena_sparql_api.batch;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;



/**
 * Phase 1: Counting...
 * Phase 2: 123/456 Datasets exported. 
 * 
 */
class ExportProgress {
    boolean isRunning;    
    boolean isFinished;
    boolean isSuccess; // Only valid, if isFinished is true

    private String message; 
    
    // True if export is in counting phase
    boolean isCounting;
    
    // Counted triples
    long maxTripleCount;
    
    // Current state of the export
    long currentTripleCount;
    
}

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
        cleanUp();
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
    
    public static void cleanUp() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SparqlExportJobConfig.class);
        JobExplorer jobExplorer = context.getBean(JobExplorer.class);
        JobRepository jobRepository = context.getBean(JobRepository.class);
        //JobOperator jobOperator = context.getBean(JobOperator.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);



//JobOperator x;
//
//x.restart(executionId)
        
        Date endTime = new Date();

        
        List<String> jobNames = jobExplorer.getJobNames();
        for(String jobName : jobNames) {
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 1000000);
            
            //Set<JobExecution> jobExecutions = jobExplorer.findRunningJobExecutions(jobName);
            
            for(JobInstance jobInstance : jobInstances) {
                List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
                
                for(JobExecution jobExecution : jobExecutions) {

                    
//                    long jobExecutionId = jobExecution.getId();
//                    try {
//                        //jobOperator.restart(jobExecutionId);
//                    } catch(Exception e) {
//                        logger.warn("Failed to restart a job", e);
//                    }
                    Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
                    for(StepExecution stepExecution : stepExecutions) {
                        BatchStatus stepStatus = stepExecution.getStatus();
                        
                        if(stepStatus.equals(BatchStatus.STARTED)) {
                            stepExecution.setStatus(BatchStatus.STOPPED);
                            stepExecution.setEndTime(endTime);
                            jobRepository.update(stepExecution);
                        }
                    }
                    
                    BatchStatus jobStatus = jobExecution.getStatus();
                    if(jobStatus.equals(BatchStatus.STARTED)) {
                        jobExecution.setStatus(BatchStatus.STOPPED);
                        jobExecution.setEndTime(endTime);
                        jobRepository.update(jobExecution);
                    }

                    
//                    if(jobExecution.getExitStatus().isRunning()) {
//                        JobParameters jobParameters = jobExecution.getJobParameters();
//                        
//                        result = jobLauncher.run();
//                    }
                    
                }
            }
        }
        
        
        //JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        //jobExplorer.findRunningJobExecutions();
    }
    

}
