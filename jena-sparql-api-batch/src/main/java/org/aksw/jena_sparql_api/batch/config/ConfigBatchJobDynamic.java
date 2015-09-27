package org.aksw.jena_sparql_api.batch.config;

import org.aksw.jena_sparql_api.batch.BatchWorkflowManager;
import org.aksw.jena_sparql_api.spring.conversion.ConverterRegistryPostProcessor;
import org.aksw.jena_sparql_api.stmt.SparqlUpdateParser;
import org.aksw.jena_sparql_api.stmt.SparqlUpdateParserImpl;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.configuration.annotation.AbstractBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;



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
@ComponentScan({"org.aksw.jena_sparql_api.spring", "org.aksw.jena_sparql_api.batch.step"})
@EnableBatchProcessing
public class ConfigBatchJobDynamic
    implements ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ConversionService conversionService() {
        ConversionService result = new DefaultConversionService();
        return result;
    }

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

    @Bean
    public SparqlUpdateParser sparqlUpdateParserDefault() {
        SparqlUpdateParser result = SparqlUpdateParserImpl.create();
        return result;
    }

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        BeanFactoryPostProcessor result = new ConverterRegistryPostProcessor();
        return result;
    }
}
