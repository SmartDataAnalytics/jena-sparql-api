package org.aksw.jena_sparql_api.batch.config;

import java.util.Arrays;

import org.aksw.jena_sparql_api.batch.BatchWorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.configuration.annotation.AbstractBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.JobScope;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;



/*
class ItemProcessorSparqlResultSet
    implements ItemProcessor<Binding, String>
{
    private Query query;

    @Override
    public String process(Binding item) throws Exception {
        // TODO: What format to write out?
        // If we had the schema, we could write out an n-rdf-terms file (although we'd still lose the isOrdered and isDistinct meta attributes)
        // Probably it would be better to analyze the query anyway


        List<String> varNames = query.getResultVars();

        return null;
    }
}
*/



/**
 *
 * Source: http://robbypelssers.blogspot.de/2013/09/spring-batch-demo.html
 *
 * NOTE DataSource must contain the spring batch schema
 *
 * .addScript("classpath:org/springframework/batch/core/schema-drop-hsqldb.sql")
 * .addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql")
 *
 *
 * Note: This class needs a datasource
 * @author raven
 */
@Configuration
@ComponentScan({"org.aksw.jena_sparql_api.batch.step"})
@EnableBatchProcessing
public class ConfigBatchJobDynamic
    implements ApplicationContextAware
{
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigBatchJobDynamic.class);

//    @Configuration
//    @Import(ConfigServicesCore.class)
//    static class ParentContext {
//
//    }

    @Bean
    @Autowired
    public JobOperator jobOperator(JobExplorer jobExplorer, JobLauncher jobLauncher, JobRepository jobRepository, ListableJobLocator jobRegistry)
    {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(jobExplorer);
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);

        return jobOperator;
    }

    @Bean
    @Autowired
    public BatchWorkflowManager manager(AbstractBatchConfiguration batchConfig) {
        BatchWorkflowManager result = new BatchWorkflowManager(batchConfig);
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)((ConfigurableApplicationContext)ctx).getBeanFactory();
        logger.info("Application context contains: " + Arrays.toString(beanFactory.getRegisteredScopeNames()));
    }

//    @Bean
//    public JobScope jobScope() {
//        JobScope result = new JobScope();
//        return result;
//    }
//
//    @Bean
//    public StepScope stepScope() {
//        StepScope result = new StepScope();
//        return result;
//    }

//    @Bean
//    @Autowired
//    // JobScope jobScope,
//    public String deleteThisTest(StepScope stepScope) {
//        //System.out.println(jobScope);
//        System.out.println(stepScope);
//        return "yay";
//    }

}
