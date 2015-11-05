package org.aksw.jena_sparql_api.batch.config;

import java.util.List;
import java.util.Random;

import org.aksw.jena_sparql_api.batch.cli.main.MainBatchWorkflow;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp;
import org.aksw.jena_sparql_api.shape.ResourceShapeParser;
import org.aksw.jena_sparql_api.shape.ResourceShapeParserImpl;
import org.aksw.jena_sparql_api.sparql.ext.http.HttpInterceptorRdfLogging;
import org.aksw.jena_sparql_api.sparql.ext.http.SinkModelWriter;
import org.aksw.jena_sparql_api.spring.conversion.ConverterRegistryPostProcessor;
import org.aksw.jena_sparql_api.stmt.SparqlConceptParser;
import org.aksw.jena_sparql_api.stmt.SparqlConceptParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlExprParser;
import org.aksw.jena_sparql_api.stmt.SparqlExprParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlUpdateParser;
import org.aksw.jena_sparql_api.stmt.SparqlUpdateParserImpl;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.jena.riot.web.HttpOp;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;

@Configuration
@ComponentScan({"org.aksw.jena_sparql_api.spring.conversion"})
public class ConfigServicesCore
    implements ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Bean
    @Qualifier("logging")
    public SparqlService defaultLoggerStore() {
        SparqlServiceFactory ssf = new SparqlServiceFactoryHttp();
        SparqlService result = ssf.createSparqlService("http://localhost:8890/sparql", DatasetDescriptionUtils.createDefaultGraph("http://jsa.aksw.org/log/"), null);
        return result;
    }

    @Bean
    @Autowired
    public Supplier<HttpClient> httpClientSupplier(@Qualifier("logging") SparqlService sparqlService) {
        SinkModelWriter sink = new SinkModelWriter(sparqlService);

        HttpInterceptorRdfLogging logger = new HttpInterceptorRdfLogging(sink);

        SystemDefaultHttpClient httpClient = new SystemDefaultHttpClient();
        httpClient.addRequestInterceptor(logger);
        httpClient.addResponseInterceptor(logger);

        // TODO This sets the httpClient globally, which is actually not desired
        HttpOp.setDefaultHttpClient(httpClient);
        HttpOp.setUseDefaultClientWithAuthentication(true);

        Supplier<HttpClient> result = Suppliers.<HttpClient>ofInstance(httpClient);

        return result;
    }

    @Bean
    public Gson defaultGson() {
        Gson result = new Gson();
        return result;
    }

    @Bean
    public ConfigurableConversionService conversionService() {
        ConfigurableConversionService result = new DefaultConversionService();
        return result;
    }

    @Bean
    public PrefixMapping defaultPrefixMapping() {
//        PrefixMapping result = new PrefixMappingImpl();
//
//        result.getNsPrefixMap().putAll(MainBatchWorkflow.getDefaultPrefixMapping().getNsPrefixMap());
        PrefixMapping result = MainBatchWorkflow.getDefaultPrefixMapping();
        return result;
    }

    @Bean
    @Autowired
    public Prologue defaultPrologue(PrefixMapping prefixMapping) {
        Prologue result = new Prologue(prefixMapping);
        return result;
    }

    @Bean
    @Autowired
    public SparqlQueryParser defaultSparqlQueryParser(Prologue prologue) {
        SparqlQueryParser result = SparqlQueryParserImpl.create(Syntax.syntaxARQ, prologue);
        return result;
    }

    @Bean
    @Autowired
    public SparqlUpdateParser defaultSparqlUpdateParser(Prologue prologue) {
        SparqlUpdateParser result = SparqlUpdateParserImpl.create(Syntax.syntaxARQ, prologue);
        return result;
    }

    @Bean
    @Autowired
    public SparqlElementParser defaultSparqlElementParser(SparqlQueryParser queryParser) {
        SparqlElementParser result = new SparqlElementParserImpl(queryParser);
        return result;
    }

    @Bean
    @Autowired
    public SparqlConceptParser defaultSparqlConceptParser(SparqlElementParser elementParser) {
        SparqlConceptParser result = new SparqlConceptParserImpl(elementParser);
        return result;
    }

    @Bean
    @Autowired
    public SparqlRelationParser defaultSparqlRelationParser(SparqlElementParser elementParser) {
        SparqlRelationParser result = new SparqlRelationParserImpl(elementParser);
        return result;
    }


    @Bean
    @Autowired
    public SparqlExprParser defaultSparqlExprParser(PrefixMapping pm) {
        SparqlExprParser result = new SparqlExprParserImpl(pm);
        return result;
    }

    private Random random = new Random();

    @Bean
    public SparqlServiceFactory defaultSparqlServiceFactory() {
        SparqlServiceFactory result = new SparqlServiceFactoryHttp();

        long jobInstanceId = random.nextLong();


//        DatasetListenerSink
//        //Fluent
//        ChangeSetMetadata metadata = new ChangeSetMetadata("claus", "testing");
//        SparqlServiceFactoryEventSource result = new SparqlServiceFactoryEventSource(ssf);
//        SinkChangeSetWriter sink = new SinkChangeSetWriter(metadata, ssfChangeSet);
//        result.getListeners().add(new DatasetListenerSink(sink));


        return result;
    }

    @Bean
    @Autowired
    public ResourceShapeParser defaultResourceShapeParser(Prologue prologue, Gson gson) {
        ResourceShapeParser result = new ResourceShapeParserImpl(prologue, gson);
        return result;
    }

    @Bean
    @Autowired
    public List<Converter<?, ?>> defaultConverters(List<Converter<?, ?>> converters) {
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        for(Object item : converters) {
            beanFactory.autowireBean(item);
        }
        return converters;
    }

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        BeanFactoryPostProcessor result = new ConverterRegistryPostProcessor();
        return result;
    }

    @Bean
    public BeanPostProcessor beanFactoryPostProcessorAutowire() {
        BeanPostProcessor result = new AutowiredAnnotationBeanPostProcessor();
        return result;
    }

}
