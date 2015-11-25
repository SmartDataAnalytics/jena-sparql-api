package org.aksw.jena_sparql_api.batch;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;

public class SparqlBatchUtils {

    public static void cleanUp(ApplicationContext context) {
            //ApplicationContext context = new AnnotationConfigApplicationContext(ConfigSparqlExportJob.class);
            JobExplorer jobExplorer = context.getBean(JobExplorer.class);
            JobRepository jobRepository = context.getBean(JobRepository.class);
            //JobOperator jobOperator = context.getBean(JobOperator.class);
            //JobLauncher jobLauncher = context.getBean(JobLauncher.class);



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

                            if(!BatchStatus.STOPPED.equals(stepStatus)) {
                                stepExecution.setStatus(BatchStatus.STOPPED);
                                stepExecution.setEndTime(endTime);
                                jobRepository.update(stepExecution);
                            }
                        }

                        BatchStatus jobStatus = jobExecution.getStatus();
                        //jobExecution.isRunning()
                        //if(jobStatus.equals(BatchStatus.STARTED)) {
                        if(!BatchStatus.STOPPED.equals(jobStatus)) {
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
