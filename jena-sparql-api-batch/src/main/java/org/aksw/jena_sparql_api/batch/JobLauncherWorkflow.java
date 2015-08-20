package org.aksw.jena_sparql_api.batch;

import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.AbstractBatchConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;

import com.google.gson.Gson;

public class JobLauncherWorkflow {

    public static final String JOB_CONTEXT = "context";
    
    private AbstractBatchConfiguration config;
    private Gson gson;

    
    public JobLauncherWorkflow(AbstractBatchConfiguration config, Gson gson) {
        this.config = config;
        this.gson = gson;
    }

    public JobExecution launchJob(String workflow) throws Exception
    {
        Map<String, Object> data = gson.fromJson(workflow, Map.class);
        //String normalized = gson.toJson(data);
        
        JobRepository jobRepository = config.jobRepository();
        JobLauncher jobLauncher = config.jobLauncher();
        Job job = null;
        

        JobParameters jobParameters = new JobParametersBuilder()
            .addString(JobLauncherWorkflow.JOB_CONTEXT, workflow, true)
            .toJobParameters();

        JobExecution result = jobRepository.getLastJobExecution(job.getName(), jobParameters);

        // If there was a prior job, return its execution context
        BatchStatus status = result == null ? null : result.getStatus();
        if(status != null) {
            if(status.isRunning() || status.equals(BatchStatus.COMPLETED)) {
                return result;
            }
        }

        result = jobLauncher.run(job, jobParameters);

        return result;
    }
/*
    public InputStream getTargetInputStream(long jobExecutionId) throws FileNotFoundException {
        JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        JobParameters jobParameters = jobExecution.getJobParameters();
        String targetResource = jobParameters.getString(ConfigSparqlExportJob.JOBPARAM_TARGET_RESOURCE);

        FileInputStream result = new FileInputStream(targetResource);
        return result;
    }


    public static SparqlExportManager createTestInstance() {
        ApplicationContext context = new AnnotationConfigApplicationContext(ConfigSparqlExportJob.class);
        JobExplorer jobExplorer = context.getBean(JobExplorer.class);
        JobRepository jobRepository = context.getBean(JobRepository.class);
        //JobOperator jobOperator = context.getBean(JobOperator.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);

        SparqlExportManager result = new SparqlExportManager(jobExplorer, jobRepository, jobLauncher, job);
        //context.
        return result;
    }
    */
    
    //{
    /*
    JobBuilder jobBuilder = jobBuilders.get("sparqlExportJob");
    SimpleJobBuilder jb = jobBuilder.start(dataCountStep(null, null));
    jb = jb.next(dataFetchStep());

    Job job = jb.build();
    */        //ApplicationContext context = new AnnotationConfigApplicationContext(SparqlExportJobConfig.class);
    //JobRepository jobRepository = context.getBean(JobRepository.class);
    //JobLauncher jobLauncher = context.getBean(JobLauncher.class);
    //Job job = context.getBean(Job.class);

//    switch(status) {
//    case COMPLETED:
//        break;
//        default
//    }
    
    //}
}
