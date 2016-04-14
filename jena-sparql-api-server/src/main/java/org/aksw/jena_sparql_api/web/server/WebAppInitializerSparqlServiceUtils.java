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
 * Note this class (with the utils suffix) no longer inherits from WebApplicationInitializer,
 * because servlets environments may wrongly pick up this class as an entry point
 * to the application.
 *
 * @author raven
 *
 */
public class WebAppInitializerSparqlServiceUtils {

    public static WebApplicationInitializer create(Class<?> config) {
         AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
         rootContext.register(config);

         WebApplicationInitializer result = create(rootContext);

        return result;
    }


    public static WebApplicationInitializer create(WebApplicationContext rootContext) {
        WebApplicationInitializer result = new WebApplicationInitializer() {

            @Override
            public void onStartup(ServletContext servletContext)
                    throws ServletException {
                init(servletContext, rootContext);
            }
        };

        return result;
    }


    public static void init(ServletContext servletContext, WebApplicationContext rootContext) {

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
