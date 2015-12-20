package org.aksw.jena_sparql_api.batch.step;

import java.util.Arrays;

import org.aksw.jena_sparql_api.batch.reader.ItemReaderQuad;
import org.aksw.jena_sparql_api.batch.reader.PredicateQuadExpr;
import org.aksw.jena_sparql_api.batch.writer.ItemWriterQuad;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.google.common.base.Predicate;
import com.hp.hpl.jena.sparql.core.Quad;


public class ConfigStepSparqlDiff {
//
//    @StepScope
//    @Autowired
//    public ItemReader<Quad> itemReader(
//            @Value("#{ stepExecutionContext[minValue] }") int minValue,
//            @Value("#{ stepExecutionContext[maxValue] }") int maxValue)
//    {
//        ItemReaderQuad result = new ItemReaderQuad();
//        result.setCurrentItemCount(minValue);
//        result.setMaxItemCount(maxValue);
//        result.setPageSize(readSize);
//        result.setQef(source);
//        result.setQuery(query);
//
//        AnnotationConfigApplicationContext x;
//        //AnnotationConfigUtils.
//        //x.re
//        return result;
//    }
//
//    @StepScope
//    @Autowired
//    public ItemReader<Quad> itemWriter() {
//        ItemWriterQuad writer = new ItemWriterQuad(target, isDelete);
//
//    }
//
//    @Override
//    protected Step configureStep(StepBuilder stepBuilder) {
//
//
//
////    	BeanDefinitionBuilder x;
////    	x.pro
//        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)((ConfigurableApplicationContext)context).getBeanFactory();
//        RootBeanDefinition itemReaderBd = new RootBeanDefinition(ItemReaderQuad.class);
//        itemReaderBd.setScope("step");
//        itemReaderBd.getPropertyValues()
//            .add("currentItemCount", "#{ stepExecutionContext[minValue] }")
//            .add("maxItemCount", "#{ stepExecutionContext[maxValue] }")
//            .add("pageSize", readSize)
//            .add("qef", source)
//            .add("query", query)
//            ;
//
//        String itemReaderName = name + "-itemReader";
//        beanFactory.registerBeanDefinition(itemReaderName, itemReaderBd);
//        System.out.println(Arrays.toString(beanFactory.getRegisteredScopeNames()));
//
//
//        String proxyName = itemReaderName + "-proxy";
//        RootBeanDefinition proxyBd = new RootBeanDefinition(ScopedProxyFactoryBean.class);
//        proxyBd.getPropertyValues()
//            .add("targetBeanName", itemReaderName);
//        beanFactory.registerBeanDefinition(proxyName, proxyBd);
//
//        Object itemReader = beanFactory.getBean(proxyName);
//        @SuppressWarnings("unchecked")
//        ItemReader<Quad> reader = (ItemReader<Quad>)itemReader;
//
////        SimpleStepFactoryBean x;
////        RootBeanDefinition stepBd = new RootBeanDefinition(SimpleStepFactoryBean.class);
////        stepBd.getPropertyValues()
////            .add("commitInterval", chunkSize)
////            .add("itemReader", )
////            .add("itemProcessor", processor)
////            .add("writer", writer)
////            ;
//
//
//        //SimpleStepFactoryBean<T, S>
//
//
////        ScopedProxyUtils.createScopedProxy(definition, registry, proxyTargetClass)
//        beanFactory.registerBeanDefinition(name + "itemReader", itemReaderBd);
//
//        //Object bean = beanFactory.getBean("mytest");
//        //Object bean = context.getBean("mytest");
//
//
//        final Predicate<Quad> predicate = filter == null ? null : new PredicateQuadExpr(filter);
//
//        //ItemReaderQuad reader = new ItemReaderQuad(source, query);
//        //reader.setPageSize(readSize);
//
//        ItemProcessor<? super Quad, ? extends Quad> processor;
//        if(predicate != null) {
//            ValidatingItemProcessor<Quad> validatingProcessor = new ValidatingItemProcessor<Quad>();
//            validatingProcessor.setValidator(new ValidatorQuadByPredicate(predicate));
//            validatingProcessor.setFilter(true);
//            try {
//                validatingProcessor.afterPropertiesSet();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            processor = validatingProcessor;
//        } else {
//            processor = new PassThroughItemProcessor<Quad>();
//        }
//
//        //ItemProcessor<? super Quad, ? extends Quad> processor = new PassThroughItemProcessor<Quad>();
//        ItemWriterQuad writer = new ItemWriterQuad(target, isDelete);
//
//        //reader.setPageSize(chunkSize);
//
//
//        //SimplePartitioner x
//
//        Step result = stepBuilder
//                .<Quad, Quad>chunk(chunkSize)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .build()
//                ;
//
//
//
//
//        if(throttle != null) {
//            Step slaveStep = result;
//
//            Partitioner partitioner = new PartitionerSparqlSlice(source, query);
//            result = stepBuilder
//                .partitioner(slaveStep)
//                .partitioner(name, partitioner)
//                .taskExecutor(taskExecutor)
//                .build()
//                ;
//
//
//            //.partitioner(name + "-partitioner", partitioner).
//        }
//
//        return result;
//    }
//
//    ApplicationContext context;
//
//    @Override
//    public void setApplicationContext(ApplicationContext context) throws BeansException {
//        this.context = context;
//        //System.out.println("My context is: " + context);
//    }
}
