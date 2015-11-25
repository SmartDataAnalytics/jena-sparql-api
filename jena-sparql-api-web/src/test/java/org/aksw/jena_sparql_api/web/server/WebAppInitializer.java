package org.aksw.jena_sparql_api.web.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.aksw.jena_sparql_api.web.utils.WebAppInitUtils;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.WebApplicationInitializer;


public class WebAppInitializer
    implements WebApplicationInitializer
{

    @Override
    public void onStartup(ServletContext servletContext)
        throws ServletException
    {
        WebAppInitUtils.defaultSetup(servletContext, ConfigApp.class);

        // Create the dispatcher servlet's Spring application context
//        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
//        dispatcherContext.register(ConfigWebMvc.class);
        ServletRegistration.Dynamic servlet = servletContext.addServlet("servletSparqlUpdate", new ServletContainer());
        servlet.setInitParameter("jersey.config.server.provider.classnames", "org.aksw.jena_sparql_api.web.server.ServletSparqlUpdate");
        servlet.addMapping("/*");
        servlet.setAsyncSupported(true);
        servlet.setLoadOnStartup(1);
    }
}
