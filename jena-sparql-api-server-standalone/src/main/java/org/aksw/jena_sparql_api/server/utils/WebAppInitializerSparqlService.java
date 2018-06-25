package org.aksw.jena_sparql_api.server.utils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.aksw.jena_sparql_api.web.server.WebAppInitializerSparqlServiceUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

public class WebAppInitializerSparqlService
    implements  WebApplicationInitializer
{
    protected WebApplicationContext rootContext;

    public WebAppInitializerSparqlService(WebApplicationContext rootContext) {
        this.rootContext = rootContext;
    }

    @Override
    public void onStartup(ServletContext servletContext)
            throws ServletException {
        WebAppInitializerSparqlServiceUtils.init(servletContext, rootContext);
    }

//    public static WebApplicationInitializer create(Class<?> config) {
//        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
//        rootContext.register(config);
//
//        WebApplicationInitializer result = new WebAppInitializerSparqlService(rootContext);
//
//        return result;
//    }
}
