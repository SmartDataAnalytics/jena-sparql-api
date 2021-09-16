package org.aksw.jena_sparql_api.web.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * A default WebMvcConfig for serving snorql
 *
 * @author raven
 *
 */
// @Configuration
public class WebMvcConfigSnorql
    extends WebMvcConfigurationSupport
{
//    @Bean
//    public ViewResolver getViewResolver() {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//        resolver.setPrefix("/WEB-INF/views/");
//        resolver.setSuffix(".html");
//        return resolver;
//    }

    @Override
    public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

//    <from>^(.*)/sparql/(?!namespaces)(.*\.(css|js|png))$</from>
//    <to type="temporary-redirect">%{context-path}/resources/snorql/$2</to>
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*").addResourceLocations("classpath:/snorql/");
    }
}