package org.aksw.jena_sparql_api.spring.conversion;

import java.util.Collection;
import java.util.Map;

import org.aksw.jena_sparql_api.batch.cli.main.AutoRegistered;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;

public class ConverterRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		registry.registerBeanDefinition("conversionService",
				BeanDefinitionBuilder.rootBeanDefinition(ConversionServiceFactoryBean.class).getBeanDefinition());
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Map<String, Object> beansWithAnnotation = beanFactory.getBeansWithAnnotation(AutoRegistered.class);
		Collection<?> converters = beansWithAnnotation.values();
		ConfigurableConversionService conversionService = (ConfigurableConversionService) beanFactory
				.getBean("conversionService");
		for (Object converter : converters) {
			conversionService.addConverter((Converter<?, ?>) converter);
		}
	}
}