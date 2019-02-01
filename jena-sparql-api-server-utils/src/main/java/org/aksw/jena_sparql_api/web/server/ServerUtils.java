package org.aksw.jena_sparql_api.web.server;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Objects;

import javax.servlet.ServletException;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;

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

        logger.debug("Trying to resolve webapp by starting from location (external form): " + externalForm);

    	Path path;
    	try {
    		// TODO This assumes that builds are done under /target/classes/
    		path = Paths.get(location.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

        // Try to detect whether we are being run from an
        // archive (uber jar / war) or just from compiled classes
        if (externalForm.endsWith("/classes/")) {
        	Path webappFolder = path.resolve("../../src/main/webapp").normalize();
            if(Files.exists(webappFolder)) {
            	externalForm = webappFolder.toString();
            }
        } else if(externalForm.endsWith("-classes.jar")) {
        	Path parent = path.getParent();
        	String rawFilename = "" + path.getFileName();
        	String filename = rawFilename.replace("-classes.jar", ".war");
        	// Try if replacing '-classes.jar' with '.war' also exists        	
        	Path warPath = parent.resolve(filename);
        	if(Files.exists(warPath)) {
        		externalForm = warPath.toString();
        	}
        }
        logger.debug("Resolved webapp location to: " + externalForm);

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
//webAppContext.setInitParameter("org.apache.tomcat.InstanceManager", "org.apache.tomcat.SimpleInstanceManager");

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

        // These lines are required to get JSP working with jetty
        // https://github.com/puppetlabs/trapperkeeper-webserver-jetty9/issues/140
        Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault( server );
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        
        
        webAppContext.setConfigurations( new Configuration[] { 
        		  new WebInfConfiguration(), 
        		  new WebXmlConfiguration(),
        		  new MetaInfConfiguration(),
        		  new PlusConfiguration(), 
        		  new JettyWebXmlConfiguration(), 
        		  new AnnotationConfiguration()
        		} );

        // If we are running not from a war but a src/main/webapp folder,
        // register the listener programmatically
        if(!externalForm.endsWith(".war")) {
        	Objects.requireNonNull(initializer, "Configuration from non-war file requires an WebAppInitializer");
        	
	        webAppContext.addLifeCycleListener(new AbstractLifeCycleListener() {
	            @Override
	            public void lifeCycleStarting(LifeCycle arg0) {
	                // WebAppInitializer initializer = new WebAppInitializer();
	                try {
	                    Context servletContext = webAppContext.getServletContext();
	                    servletContext.setExtendedListenerTypes(true);
	                    // servletContext.setExtendedListenerTypes(true);
	                    initializer.onStartup(servletContext);
	                } catch (ServletException e) {
	                    throw new RuntimeException(e);
	                }
	            }
	        });
        }

        webAppContext.setServer(server);
        webAppContext.setContextPath("/");

        // context.setDescriptor(externalForm + "/WEB-INF/web.xml");
        webAppContext.setWar(externalForm);

        
      //webAppContext.setInitParameter("org.apache.tomcat.InstanceManager", "org.apache.tomcat.SimpleInstanceManager");

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
