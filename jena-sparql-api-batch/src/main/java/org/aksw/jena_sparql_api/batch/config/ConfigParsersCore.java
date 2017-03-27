package org.aksw.jena_sparql_api.batch.config;

import java.util.List;

import org.aksw.jena_sparql_api.batch.cli.main.MainBatchWorkflow;
import org.aksw.jena_sparql_api.shape.ResourceShapeParser;
import org.aksw.jena_sparql_api.shape.ResourceShapeParserImpl;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
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

import com.google.gson.Gson;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;

@Configuration
@ComponentScan({"org.aksw.jena_sparql_api.spring.conversion"})
public class ConfigParsersCore
    implements ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
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

    @Bean
    @Autowired
    public ResourceShapeParser defaultResourceShapeParser(Prologue prologue, Gson gson) {
        ResourceShapeParser result = new ResourceShapeParserImpl(prologue, gson);
        return result;
    }

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        BeanFactoryPostProcessor result = new ConverterRegistryPostProcessor();
        return result;
    }

    @Bean
    public BeanPostProcessor beanFactoryPostProcessorAutowire() {
        BeanPostProcessor result = new AutowiredAnnotationBeanPostProcessor();
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

}
