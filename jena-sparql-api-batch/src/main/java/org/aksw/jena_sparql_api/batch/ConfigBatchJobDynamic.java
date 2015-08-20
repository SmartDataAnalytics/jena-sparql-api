package org.aksw.jena_sparql_api.batch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.configuration.annotation.AbstractBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.engine.binding.Binding;



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
@EnableBatchProcessing
public class ConfigBatchJobDynamic {

    /**
     * Attention: You have to name these beans jobBuilders and stepbuilders
     * respectively. See
     * http://docs.spring.io/spring-batch/reference/html/configureJob.html
     */

    public static final String JOBPARAM_SERVICE_URI = "serviceUri";
    public static final String JOBPARAM_DEFAULT_GRAPH_URIS = "defaultGraphUris";
    public static final String JOBPARAM_NAMED_GRAPH_URIS = "namedGraphUris";
    public static final String JOBPARAM_QUERY_STRING = "queryString";

    public static final String JOBPARAM_TARGET_RESOURCE = "targetResource";

//    @Autowired
//    private JobExplorer jobExplorer;

    
    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;


    private static final int chunkSize = 1000;
//    @Autowired
//    private DataSource dataSource;

    @Bean
    @Autowired
    public JobExplorerFactoryBean jobExplorer(DataSource dataSource) {
        JobExplorerFactoryBean result = new JobExplorerFactoryBean();
        result.setDataSource(dataSource);

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
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(5);

        return taskExecutor;
    }


    @Bean
    @Autowired
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor());

        return jobLauncher;
    }

    @Bean
    public Job sparqlExportJob() {
        JobBuilder jobBuilder = jobBuilders.get("sparqlExportJob");
        SimpleJobBuilder jb = jobBuilder.start(dataCountStep(null, null));
        //jb.next((Step)null).next((JobExecutionDecider)null).split(null).add(flows)
        jb = jb.next(dataFetchStep());

        Job job = jb.build();
        return job;
    }

    @Bean
    @JobScope
    //@StepScope
    public Query query(@Value("#{jobParameters[queryString]}") String queryString) {
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        return query;
    }


    @Bean
    @JobScope
    //@StepScope
    public QueryExecutionFactory sparqlService(
            @Value("#{jobParameters[serviceUri]}") String serviceUri,
            @Value("#{jobParameters[defaultGraphUris]}") String defaultGraphUris)
    {
        String[] tmp = defaultGraphUris.split(" ");
        List<String> dgus = Arrays.asList(tmp);

        // TODO Add named graph support
        List<String> ngus = new ArrayList<String>();

        // TODO Add authenticator support
        DatasetDescription datasetDescription = new DatasetDescription(dgus, ngus);
        SparqlServiceFactory ssf = new SparqlServiceFactoryHttp();
        SparqlService sparqlService = ssf.createSparqlService(serviceUri, datasetDescription, null);
        
        //SparqlService sparqlService = sparqlServiceFactory.createSparqlService(serviceUri, datasetDescription, null);
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();

        return qef;
    }

    @Bean
    //@StepScope
    @JobScope
    @Autowired
    public Step dataCountStep(Query query, QueryExecutionFactory qef) {
        Tasklet tasklet = new TaskletSparqlCountData(query, qef);
        return stepBuilders.get("dataCountStep").tasklet(tasklet).build();
    }

//    @Bean
//    public Step countStep {
//        stepBuilders.get("countStep").tasklet();
//    }

    @Bean
    public Step dataFetchStep() {
        return stepBuilders.get("dataFetchStep").<Binding, Binding> chunk(chunkSize)
                .reader(reader(null, null, null))
                .processor(processor())
                .writer(writer(null, null))
                .build();
    }


    @Bean
    @StepScope
    @Autowired
    public ItemReaderSparqlPaging<Binding> reader(
            Query query,
            QueryExecutionFactory qef,
//            @Value("#{jobParameters[serviceUri]}") String serviceUri,
//            @Value("#{jobParameters[defaultGraphUris]}") String defaultGraphUris,
            @Value("#{jobParameters[queryString]}") String queryString)
    {
        ItemReaderSparqlPaging<Binding> itemReader = new ItemReaderSparqlPaging<Binding>();

        itemReader.setSparqlService(qef);
        itemReader.setBindingMapper(new BindingMapperPassThrough());

        itemReader.setQuery(query);
        itemReader.setPageSize(chunkSize);
        itemReader.setSaveState(true);
        //itemReader.setQueryString(queryString);
//        itemReader.setServiceUri(serviceUri);
//        itemReader.setDefaultGraphUris(dgus);


        return itemReader;
    }

    @Bean
    @StepScope
    @Autowired
    public ResourceAwareItemWriterItemStream<Binding> writer(
            final Query query,
            @Value("#{jobParameters[targetResource]}") String targetResource)
    {
        FlatFileItemWriter<Binding> itemWriter = new FlatFileItemWriter<Binding>();

        itemWriter.setResource(new FileSystemResource(targetResource));
        itemWriter.setLineAggregator(lineAggregator(null));
        itemWriter.setEncoding("UTF-8");
        itemWriter.setSaveState(true);


        itemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            /**
             * <sparql xmlns="http://www.w3.org/2005/sparql-results#" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.w3.org/2001/sw/DataAccess/rf1/result2.xsd">
             * <head>
             *  <variable name="s"/>
             *  <variable name="p"/>
             *  <variable name="o"/>
             * </head>
             * <results distinct="false" ordered="true">
             * @see org.springframework.batch.item.file.FlatFileHeaderCallback#writeHeader(java.io.Writer)
             */
            @Override
            public void writeHeader(Writer writer) throws IOException {
                PrintWriter pw = new PrintWriter(writer);
                pw.println("<?xml version=\"1.0\"?>") ;
                pw.println("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/sw/DataAccess/rf1/result2.xsd\">");
                pw.println(" <head>") ;

                List<String> varNames = query.getResultVars();
                for(String varName : varNames) {
                    pw.println("  <variable name=\"" + varName + "\" />") ;
                }

                pw.println(" </head>") ;
                pw.print(" <results distinct=\"" + query.isDistinct() + "\" " + "ordered=\"" + !CollectionUtils.isEmpty(query.getOrderBy()) + "\">");
                pw.flush();
            }
        });

        itemWriter.setFooterCallback(new FlatFileFooterCallback() {
            /**
             *   </results>
             * </sparql>
             */
            @Override
            public void writeFooter(Writer writer) throws IOException {
                PrintWriter pw = new PrintWriter(writer);
                pw.println(" </results>") ;
                pw.println("</sparql>");
                pw.flush();
            }
        });

        return itemWriter;
    }


    @Bean
    @StepScope
    @Autowired
    public LineAggregator<Binding> lineAggregator(Query query) {
        List<String> varNames = query.getResultVars();
        LineAggregator<Binding> result = new LineAggregatorBindingToXml(varNames);

        return result;
//        return new LineAggregator<Binding>() {
//            @Override
//            public String aggregate(Binding item) {
//                return item.toString();
//            }
//        };
    }

    @Bean
    @StepScope
    @Autowired
    public ItemProcessor<Binding, Binding> processor() {
        return new PassThroughItemProcessor<Binding>();
    }

}
