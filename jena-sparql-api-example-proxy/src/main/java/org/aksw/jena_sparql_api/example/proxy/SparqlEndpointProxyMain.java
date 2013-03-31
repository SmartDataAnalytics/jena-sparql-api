package org.aksw.jena_sparql_api.example.proxy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.servlet.ServletContainer;


public class SparqlEndpointProxyMain {
	/**
	 * @param exitCode
	 */
	public static void printHelpAndExit(int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(SparqlEndpointProxyMain.class.getName(), cliOptions);
		System.exit(exitCode);
	}

	private static final Logger logger = LoggerFactory
			.getLogger(SparqlEndpointProxyMain.class);
	private static final Options cliOptions = new Options();

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		logger.info("Launching server...");

		/*
		PropertyConfigurator.configure("log4j.properties");
		LogManager.getLogManager().readConfiguration(
				new FileInputStream("jdklog.properties"));
		*/

		CommandLineParser cliParser = new GnuParser();

		cliOptions.addOption("P", "port", true, "Server port");
		//cliOptions.addOption("C", "context", true, "Context e.g. /sparqlify");
		//cliOptions.addOption("B", "backlog", true, "Maximum number of connections");

		cliOptions.addOption("s", "default service uri", true, "");
		cliOptions.addOption("o", "allow override of default service uri", true, "");
		
		CommandLine commandLine = cliParser.parse(cliOptions, args);

		
		// Parsing of command line args
		String portStr = commandLine.getOptionValue("P", "5522");
		//String backLogStr = commandLine.getOptionValue("B", "100");
		//String contextStr = commandLine.getOptionValue("C", "/sparqlify");
		int port = Integer.parseInt(portStr);
		//int backLog = Integer.parseInt(backLogStr);
		
		
		String defaultServiceUri = commandLine.getOptionValue("s", "http://localhost/sparql");
		String allowOverrideServiceUriStr = commandLine.getOptionValue("o", "false");
		Boolean allowOverrideServiceUri = Boolean.parseBoolean(allowOverrideServiceUriStr);


		
		//ServletHolder sh = new ServletHolder(ServletContainer.class);
		ServletHolder sh = new ServletHolder(org.atmosphere.cpr.AtmosphereServlet.class);
		//sh.setHeldClass(org.atmosphere.cpr.AtmosphereServlet.class);
		sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
		sh.setInitParameter(
				"com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		sh.setInitParameter("com.sun.jersey.config.property.packages",
				"org.aksw.jena_sparql_api.example.proxy");
	

		Server server = new Server(port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		//Context context = new Context(server, "/", Context.SESSIONS);
		//context.addServlet(sh, "/*");
		

		context.addServlet(sh, "/*");

		
		context.setAttribute("defaultServiceUri", defaultServiceUri);
		context.setAttribute("allowOverrideServiceUri", allowOverrideServiceUri);

		server.start();
	}

}
