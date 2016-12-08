package org.aksw.jena_sparql_api.batch.config;

import java.util.Date;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.Assert;

/**
 * At least up to spring batch 3.0.4 its not possible to create a job instance
 * without any executions; doing so will cause a null pointer exception when calling
 * jobLauncher.run().
 *
 *
 * @author raven
 *
 */
public class SimpleJobRepositoryFix
    extends SimpleJobRepository
{
    // Note: we have to duplicate all these attributes from the base class because they are
    // private there
    private JobInstanceDao jobInstanceDao;

    private JobExecutionDao jobExecutionDao;

//    private StepExecutionDao stepExecutionDao;

    private ExecutionContextDao ecDao;


    public SimpleJobRepositoryFix(JobInstanceDao jobInstanceDao, JobExecutionDao jobExecutionDao,
            StepExecutionDao stepExecutionDao, ExecutionContextDao ecDao) {
        super(jobInstanceDao, jobExecutionDao, stepExecutionDao, ecDao);

        this.jobInstanceDao = jobInstanceDao;
        this.jobExecutionDao = jobExecutionDao;
//        this.stepExecutionDao = stepExecutionDao;
        this.ecDao = ecDao;
    }

    @Override
    public JobExecution createJobExecution(String jobName, JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {

        Assert.notNull(jobName, "Job name must not be null.");
        Assert.notNull(jobParameters, "JobParameters must not be null.");

        /*
         * Find all jobs matching the runtime information.
         *
         * If this method is transactional, and the isolation level is
         * REPEATABLE_READ or better, another launcher trying to start the same
         * job in another thread or process will block until this transaction
         * has finished.
         */

        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobName, jobParameters);
        ExecutionContext executionContext = null;

        // existing job instance found
        if (jobInstance != null) {

            List<JobExecution> executions = jobExecutionDao.findJobExecutions(jobInstance);

            // check for running executions and find the last started
            for (JobExecution execution : executions) {
                if (execution.isRunning()) {
                    throw new JobExecutionAlreadyRunningException("A job execution for this job is already running: "
                            + jobInstance);
                }

                BatchStatus status = execution.getStatus();
                if (execution.getJobParameters().getParameters().size() > 0 && (status == BatchStatus.COMPLETED || status == BatchStatus.ABANDONED)) {
                    throw new JobInstanceAlreadyCompleteException(
                            "A job instance already exists and is complete for parameters=" + jobParameters
                            + ".  If you want to run this job again, change the parameters.");
                }
            }

            JobExecution lastJobExecution = jobExecutionDao.getLastJobExecution(jobInstance);
            if(lastJobExecution != null) {
                executionContext = ecDao.getExecutionContext(lastJobExecution);
            }
        }
        else {
            // no job found, create one
            jobInstance = jobInstanceDao.createJobInstance(jobName, jobParameters);
        }

        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }

        JobExecution jobExecution = new JobExecution(jobInstance, jobParameters, null);
        jobExecution.setExecutionContext(executionContext);
        jobExecution.setLastUpdated(new Date(System.currentTimeMillis()));

        // Save the JobExecution so that it picks up an ID (useful for clients
        // monitoring asynchronous executions):
        jobExecutionDao.saveJobExecution(jobExecution);
        ecDao.saveExecutionContext(jobExecution);

        return jobExecution;

    }

}
