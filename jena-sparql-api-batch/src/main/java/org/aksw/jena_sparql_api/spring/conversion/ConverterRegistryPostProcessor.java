package org.aksw.jena_sparql_api.spring.conversion;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;

public class ConverterRegistryPostProcessor
    implements BeanDefinitionRegistryPostProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(ConverterRegistryPostProcessor.class);

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .rootBeanDefinition(ConversionServiceFactoryBean.class)
                //.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE)
                .getBeanDefinition();

        registry.registerBeanDefinition("conversionService", beanDefinition);
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, Object> beansWithAnnotation = beanFactory.getBeansWithAnnotation(AutoRegistered.class);
        Collection<?> converters = beansWithAnnotation.values();
        ConfigurableConversionService conversionService = (ConfigurableConversionService) beanFactory
                .getBean("conversionService");

        for (Object converter : converters) {
            logger.debug("AutoRegistered converter: " + converter.getClass() + " - " + converter);
            conversionService.addConverter((Converter<?, ?>) converter);
        }
        logger.debug("AutoRegistered " + converters.size() + " converters");
    }
}
