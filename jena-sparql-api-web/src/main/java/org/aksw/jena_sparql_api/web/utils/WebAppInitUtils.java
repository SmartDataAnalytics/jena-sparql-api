package org.aksw.jena_sparql_api.web.utils;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

import org.aksw.jena_sparql_api.web.filters.CorsFilter;
import org.aksw.jena_sparql_api.web.filters.FilterPost;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

public class WebAppInitUtils {
    /**
     * Set up default filters for character encoding and cors
     *
     * @param servletContext
     */
    public static void defaultSetup(ServletContext servletContext, Class<?> appConfig) {
        // Create the 'root' Spring application context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(appConfig);
        //rootContext.register(ConfigApp.class);
        defaultSetup(servletContext, rootContext);
    }

    public static void defaultSetup(ServletContext servletContext, WebApplicationContext rootContext) {

//
//        // Manage the lifecycle of the root application context
        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.addListener(new RequestContextListener());

        // !!! Force UTF8 encoding !!!
        {
            FilterRegistration.Dynamic fr = servletContext.addFilter("CharacterEncodingFilter", new CharacterEncodingFilter());
            fr.setInitParameter("encoding", "UTF-8");
            fr.setInitParameter("forceEncoding", "true");
            fr.addMappingForUrlPatterns(null, true, "/*");
            fr.setAsyncSupported(true);
        }

        {
            FilterRegistration.Dynamic fr = servletContext.addFilter("CorsFilter", new CorsFilter());
            fr.addMappingForUrlPatterns(null, true, "/*");
            fr.setAsyncSupported(true);
        //  fr.setInitParameter("dispatcher", "REQUEST");
        }

        {
            FilterRegistration.Dynamic fr = servletContext.addFilter("FilterPost", new FilterPost());
            fr.addMappingForUrlPatterns(null, true, "/*");
            fr.setAsyncSupported(true);
        //  fr.setInitParameter("dispatcher", "REQUEST");
        }
    }

}
