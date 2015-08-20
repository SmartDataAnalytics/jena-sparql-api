package org.aksw.jena_sparql_api.batch;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Map;

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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class BatchWorkflowManager {
    private JobExplorer jobExplorer;
    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private Job job;

    public void processWorkflow(String workflow) {
        Gson gson = new Gson();
        
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        
        gson.fromJson(workflow, type);
    }
    

    public BatchWorkflowManager(JobExplorer jobExplorer, JobRepository jobRepository, JobLauncher jobLauncher, Job job) {
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    public JobExecution launchWorkflowJob(String workflowDesc) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException
    {
        JobParameters jobParameters = new JobParametersBuilder()
            .addString(ConfigSparqlExportJob.JOBPARAM_SERVICE_URI, workflowDesc, true)
            .toJobParameters();

        JobExecution result = jobRepository.getLastJobExecution(job.getName(), jobParameters);

        // If there was a prior job, return its execution context
        BatchStatus status = result == null ? null : result.getStatus();
        if(status == null || !(status.isRunning() || status.equals(BatchStatus.COMPLETED))) {
            result = jobLauncher.run(job, jobParameters);
        }

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
*/

    public static BatchWorkflowManager createTestInstance() throws ScriptException, SQLException {
        EmbeddedDatabaseBuilder edb = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase ed = edb
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
            .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
            .build();
            ;
            
        // SDBConnectionDesc
        

        /*
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://localhost:5432/usecase");
        ds.setUsername("postgres");
        ds.setPassword("########");
        
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("/org/aksw/jena_sparql_api/cache/cache-schema-pgsql.sql"));
        rdp.populate(ds.getConnection());
        */
        
                
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        ConfigurableApplicationContext cac = (ConfigurableApplicationContext)context;
        
        ConfigurableListableBeanFactory beanFactory = cac.getBeanFactory();
        beanFactory.registerSingleton(ed.getClass().getCanonicalName(), ed);
        
        context.register(ConfigBatchJobDynamic.class);
        context.refresh();
        
        JobExplorer jobExplorer = context.getBean(JobExplorer.class);
        JobRepository jobRepository = context.getBean(JobRepository.class);
        //JobOperator jobOperator = context.getBean(JobOperator.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);

        BatchWorkflowManager result = new BatchWorkflowManager(jobExplorer, jobRepository, jobLauncher, job);
        //context.
        return result;
    }
}