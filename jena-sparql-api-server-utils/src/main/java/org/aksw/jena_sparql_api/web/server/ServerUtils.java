package org.aksw.jena_sparql_api.web.server;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;

import javax.servlet.ServletException;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 *
 *
 * http://stackoverflow.com/questions/10738816/deploying-a-servlet-
 * programmatically-with-jetty
 * http://stackoverflow.com/questions/3718221/add-resources
 * -to-jetty-programmatically
 *
 * @author raven
 *
 *         http://kielczewski.eu/2013/11/using-embedded-jetty-spring-mvc/
 */
public class ServerUtils {

    private static final Logger logger = LoggerFactory.getLogger(ServerUtils.class);

    public static Server startServer(int port,
            WebApplicationInitializer initializer) {
        // Not sure if using this class always works as expected
        Server result = startServer(ServerUtils.class, port, initializer);
        return result;
    }

    public static Server startServer(Class<?> clazz, int port,
            WebApplicationInitializer initializer)
    {
        String externalForm = getExternalForm(clazz);
        Server result = startServer(port, externalForm, initializer);
        return result;
    }

    public static Server startServer(int port, String externalForm,
            final WebApplicationInitializer initializer) {
        Server server = prepareServer(port, externalForm, initializer);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return server;
    }

    public static Server prepareServer(int port,
            WebApplicationInitializer initializer) {
        // Not sure if using this class always works as expected
        Server result = prepareServer(ServerUtils.class, port, initializer);
        return result;
    }

    public static String getExternalForm(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        String externalForm = location.toExternalForm();

        logger.debug("External form: " + externalForm);

        // Try to detect whether we are being run from an
        // archive (uber jar / war) or just from compiled classes
        if (externalForm.endsWith("/classes/")) {

            String test = "src/main/webapp";
            File file = new File(test);
            if(file.exists()) {
                externalForm = test;
            }
        }
        return externalForm;
    }

    public static Server prepareServer(Class<?> clazz, int port,
            WebApplicationInitializer initializer) {

        String externalForm = getExternalForm(clazz);

        logger.debug("Loading webAppContext from " + externalForm);

        Server result = prepareServer(port, externalForm, initializer);
        return result;
    }

    public static Server prepareServer(int port, String externalForm,
            final WebApplicationInitializer initializer) {
        Server server = new Server(port);
        // server.setHandler(getServletContextHandler(getContext()));

        // SocketConnector connector = new SocketConnector();
        //
        // // Set some timeout options to make debugging easier.
        // connector.setMaxIdleTime(1000 * 60 * 60);
        // connector.setSoLingerTime(-1);
        // connector.setPort(port);
        // server.setConnectors(new Connector[] { connector });

        final WebAppContext webAppContext = new WebAppContext();

        // AnnotationConfigWebApplicationContext rootContext = new
        // AnnotationConfigWebApplicationContext();
        // rootContext.register(AppConfig.class);
        //
        // // Manage the lifecycle of the root application context
        // webAppContext.addEventListener(new
        // ContextLoaderListener(rootContext));
        // webAppContext.addEventListener(new RequestContextListener());

        // webAppContext.addEventListener(new ContextLoaderListener(context);
        // Context servletContext = webAppContext.getServletContext();

        webAppContext.addLifeCycleListener(new AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle arg0) {
                // WebAppInitializer initializer = new WebAppInitializer();
                try {
                    Context servletContext = webAppContext.getServletContext();
                    // servletContext.setExtendedListenerTypes(true);
                    initializer.onStartup(servletContext);
                } catch (ServletException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        webAppContext.setServer(server);
        webAppContext.setContextPath("/");

        // context.setDescriptor(externalForm + "/WEB-INF/web.xml");
        webAppContext.setWar(externalForm);

        server.setHandler(webAppContext);
        return server;
    }

    // public void mainGrizzly() {
    // HttpServer server = new HttpServer();
    //
    // final NetworkListener listener = new NetworkListener("grizzly",
    // NetworkListener.DEFAULT_NETWORK_HOST, PACS.RESTPort);
    // server.addListener(listener);
    //
    // ResourceConfig rc = new ResourceConfig();
    // rc.packages("org.aksw.facete2.web");
    // HttpHandler processor =
    // ContainerFactory.createContainer(GrizzlyHttpContainer.class, rc);
    // server.getServerConfiguration().addHttpHandler(processor, "");
    // }
}
