package org.aksw.jena_sparql_api.batch;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.ListableJobLocator;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
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
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
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
import com.hp.hpl.jena.sparql.engine.binding.Binding;

class LineAggregatorBindingToXml
    implements LineAggregator<Binding> {

    public Collection<String> varNames;
    
    public LineAggregatorBindingToXml(Collection<String> varNames) {
        this.varNames = varNames;
    }
    
    @Override
    public String aggregate(Binding binding) {
        String result = "  " + ResultSetXmlUtils.toXmlStringBinding(binding, varNames);
        return result;
    }
}


class DataCountTasklet
    extends StepExecutionListenerSupport implements Tasklet, InitializingBean 
{
    public static final String KEY = DataCountTasklet.class.getSimpleName() + ".count";

    private Query query;
    private QueryExecutionFactory sparqlService;
    
    public DataCountTasklet(Query query, QueryExecutionFactory sparqlService) {
        this.query = query;
        this.sparqlService = sparqlService;
    }
    
    
    @Override
    public RepeatStatus execute(StepContribution contribution,
            ChunkContext chunkContext) throws Exception {
        
        long count = QueryExecutionUtils.countQuery(query, sparqlService);
        
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(KEY, count);

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }   
}

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
 * @author raven
 * 
 */
@Configuration
@EnableBatchProcessing
//@Import(SparqlServiceFactoryConfig.class)
public class SparqlExportJobConfig {

    /**
     * Attention: You have to name these beans jobBuilders and stepbuilders
     * respectively. See
     * http://docs.spring.io/spring-batch/reference/html/configureJob.html
     */

    public static final String JOBPARAM_SERVICE_URI = "serviceUri";
    public static final String JOBPARAM_DEFAULT_GRAPH_URIS = "defaultGraphUris";
    public static final String JOBPARAM_QUERY_STRING = "queryString";
    
    public static final String JOBPARAM_TARGET_RESOURCE = "targetResource";

//    @Autowired
//    private JobExplorer jobExplorer;

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    @Autowired
    private SparqlServiceFactory sparqlServiceFactory;
    
    
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
        jb = jb.next(dataFetchStep());
                
        Job job = jb.build();
        return job;
    }

    @Bean
    @Autowired
    public Step dataCountStep(Query query, QueryExecutionFactory sparqlService) {
        Tasklet tasklet = new DataCountTasklet(query, sparqlService);
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
    public Query query(@Value("#{jobParameters[queryString]}") String queryString) {
        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
        return query;
    }

    
    @Bean
    @StepScope
    public QueryExecutionFactory sparqlService(
            @Value("#{jobParameters[serviceUri]}") String serviceUri,
            @Value("#{jobParameters[defaultGraphUris]}") String defaultGraphUris)
    {
        String[] tmp = defaultGraphUris.split(" ");
        Collection<String> dgus = Arrays.asList(tmp);

        QueryExecutionFactory sparqlService = sparqlServiceFactory.createSparqlService(serviceUri, dgus);

        return sparqlService;
    }
    
    @Bean
    @StepScope
    @Autowired
    public SparqlPagingItemReader<Binding> reader(
            Query query,
            QueryExecutionFactory sparqlService,
//            @Value("#{jobParameters[serviceUri]}") String serviceUri,
//            @Value("#{jobParameters[defaultGraphUris]}") String defaultGraphUris,
            @Value("#{jobParameters[queryString]}") String queryString)
    {        
        SparqlPagingItemReader<Binding> itemReader = new SparqlPagingItemReader<Binding>();
                
        itemReader.setSparqlService(sparqlService);
        itemReader.setBindingMapper(new BindingMapperPassThrough());
        
        itemReader.setPageSize(chunkSize);
        itemReader.setSaveState(true);
        itemReader.setQuery(query);
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

    
    /*
    public static String toXmlStringBinding(Binding binding) {
        binding.v
        String result = toXmlStringBinding(binding)
    }
    */
    
}
