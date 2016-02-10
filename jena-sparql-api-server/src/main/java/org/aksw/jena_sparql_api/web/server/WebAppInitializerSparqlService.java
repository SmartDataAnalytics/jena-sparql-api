package org.aksw.jena_sparql_api.web.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.aksw.jena_sparql_api.web.utils.WebAppInitUtils;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * Convenience WebAppInitializer for setting up a SPARQL service
 *
 * @author raven
 *
 */
public class WebAppInitializerSparqlService
    implements WebApplicationInitializer
{
    protected WebApplicationContext rootContext;

    public static WebAppInitializerSparqlService create(Class<?> appConfig) {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(appConfig);

        WebAppInitializerSparqlService result = new WebAppInitializerSparqlService(rootContext);
        return result;
    }

    public WebAppInitializerSparqlService(WebApplicationContext rootContext) {
        this.rootContext = rootContext;
    }

    @Override
    public void onStartup(ServletContext servletContext)
        throws ServletException
    {
        WebAppInitUtils.defaultSetup(servletContext, rootContext);

        {
            ServletRegistration.Dynamic servlet = servletContext.addServlet("sparqlServiceServlet", new ServletContainer());
            servlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, "org.aksw.jena_sparql_api.web.servlets.ServletSparqlServiceImpl");
//            servlet.setInitParameter(ServletProperties.FILTER_FORWARD_ON_404, "true");
//            servlet.setInitParameter(ServletProperties.FILTER_STATIC_CONTENT_REGEX, ".*(html|css|js)");
            servlet.addMapping("/sparql/*");
            servlet.setAsyncSupported(true);
            servlet.setLoadOnStartup(1);
        }

        {
            AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
            dispatcherContext.register(WebMvcConfigSnorql.class);

            ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherServlet", new DispatcherServlet(dispatcherContext));
            servlet.addMapping("/*");
            servlet.setAsyncSupported(true);
            servlet.setLoadOnStartup(1);
        }

    }
}
