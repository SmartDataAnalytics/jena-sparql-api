package org.aksw.jena_sparql_api.web.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.aksw.jena_sparql_api.web.utils.WebAppInitUtils;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
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
    implements WebApplicationInitializer, ApplicationContextAware
{
    protected WebApplicationContext rootContext;

    public WebAppInitializerSparqlService() {
        this(null);
    }

    public WebAppInitializerSparqlService(WebApplicationContext rootContext) {
        super();
        this.rootContext = rootContext;
    }

    @Override
    public void onStartup(ServletContext servletContext)
        throws ServletException
    {
        Assert.isTrue(this.rootContext != null);

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

    @Override
    public void setApplicationContext(ApplicationContext rootContext)
            throws BeansException {
        Assert.isNull(this.rootContext, "root context was already set");

        this.rootContext = (WebApplicationContext)rootContext;
    }


    public static WebAppInitializerSparqlService create(Class<?> appConfig) {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(appConfig);

        WebAppInitializerSparqlService result = new WebAppInitializerSparqlService(rootContext);
        return result;
    }

}
