package org.aksw.jena_sparql_api.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Joiner;

public class SparqlExportManager {
    private JobExplorer jobExplorer;
    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private Job job;
    

    public SparqlExportManager(JobExplorer jobExplorer, JobRepository jobRepository, JobLauncher jobLauncher, Job job) {
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobLauncher = jobLauncher;
        this.job = job;
    }
    
    public JobExecution launchSparqlExport(String serviceUri, Collection<String> defaultGraphUris, String queryString, String targetResource) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException
    {
        //ApplicationContext context = new AnnotationConfigApplicationContext(SparqlExportJobConfig.class);
        //JobRepository jobRepository = context.getBean(JobRepository.class);
        //JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        //Job job = context.getBean(Job.class);

        
        Set<String> tmp = new TreeSet<String>(defaultGraphUris);
        String dgu = Joiner.on(' ').join(tmp);
        
        JobParameters jobParameters = new JobParametersBuilder()
            .addString(SparqlExportJobConfig.JOBPARAM_SERVICE_URI, serviceUri, true)
            .addString(SparqlExportJobConfig.JOBPARAM_DEFAULT_GRAPH_URIS, dgu, true)
            .addString(SparqlExportJobConfig.JOBPARAM_QUERY_STRING, queryString, true)
            .addString(SparqlExportJobConfig.JOBPARAM_TARGET_RESOURCE, targetResource, true)
            .toJobParameters();

        JobExecution result = jobRepository.getLastJobExecution(job.getName(), jobParameters);
        
        // If there was a prior job, return its execution context
        BatchStatus status = result == null ? null : result.getStatus();
        if(status != null) {
            if(status.isRunning() || status.equals(BatchStatus.COMPLETED)) {
                return result;
            }
        }

//        switch(status) {
//        case COMPLETED:
//            break;
//            default
//        }
        
        result = jobLauncher.run(job, jobParameters);
       
        return result;
    }    
    
    public InputStream getTargetInputStream(long jobExecutionId) throws FileNotFoundException {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        JobParameters jobParameters = jobExecution.getJobParameters();
        String targetResource = jobParameters.getString(SparqlExportJobConfig.JOBPARAM_TARGET_RESOURCE);
        
        FileInputStream result = new FileInputStream(targetResource);
        return result;
    }
    
    
    public static SparqlExportManager createTestInstance() {

        ApplicationContext context = new AnnotationConfigApplicationContext(SparqlExportJobConfig.class);
        JobExplorer jobExplorer = context.getBean(JobExplorer.class);
        JobRepository jobRepository = context.getBean(JobRepository.class);
        //JobOperator jobOperator = context.getBean(JobOperator.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);

        SparqlExportManager result = new SparqlExportManager(jobExplorer, jobRepository, jobLauncher, job);
        //context.
        return result;
    }
}