package org.aksw.jena_sparql_api.ext.virtuoso;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.connection.SparqlUpdateConnectionJsa;
import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.io.utils.AbstractSystemService;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service wrapper instance manages a single underlying service process.
 *
 * @author raven
 *
 */
public class VirtuosoSystemService
    extends AbstractSystemService
    implements SparqlBasedSystemService
{
    private static final Logger logger = LoggerFactory.getLogger(VirtuosoSystemService.class);

    protected Path virtExecPath;
    protected Path virtIniPath;

    // The working directory. By default, the folder of the ini file
    protected Path workingPath;

    // The parsed ini file, used to read out ports
    protected transient Ini virtIni;
    protected transient Preferences virtIniPrefs;


    public VirtuosoSystemService(Path virtExecPath, Path virtIniPath) {
        super();
        this.virtExecPath = virtExecPath;
        this.virtIniPath = virtIniPath;

        this.workingPath = virtIniPath.getParent();
    }

    @Override
    public VirtuosoSystemService setOutputSink(Consumer<String> outputSink) {
        super.setOutputSink(outputSink);
        return this;
    }

    @Override
    public VirtuosoSystemService setHealthCheckInterval(Duration healthCheckInterval) {
        super.setHealthCheckInterval(healthCheckInterval);
        return this;
    }

    @Override
    protected ProcessBuilder prepareProcessBuilder() {
        ProcessBuilder result = new ProcessBuilder(virtExecPath.toString(), "-c", virtIniPath.toString(), "-f");
        result.directory(workingPath.toFile());
        return result;
    }

    /**
     * Starts the service and yields a future indicating whether it became
     * healthy within a certain time limit.
     *
     * The service may still become healthy at a later stage, however,
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void startUp() throws IOException, InterruptedException {
        // Attempt to read the ini file
        virtIni = new Ini(virtIniPath.toFile());
        virtIniPrefs = new IniPreferences(virtIni);

        setOutputSink(logger::info);

        super.startUp();
    }


    /**
     * Perform a health check on the service
     *
     * @return
     */
    public boolean performHealthCheck() {
        boolean result = false;
        try (RDFConnection conn = createDefaultConnection()) {

            // A very basic select query, which all SPARQL systems
            // should be capable to process quickly, as usually it does not
            // match anything
            conn.querySelect("SELECT * { <http://example.org/healthcheck> a ?t }", (qs) -> {
            });
            result = true;
        } catch (Exception e) {
            logger.debug("Health check failed with reason: ", e);
        }

        logger.debug("Health check response: " + result);

        return result;
    }

    public static RDFConnection connectVirtuoso(String host, int httpPort, int odbcPort) {
        String endpointUrl = "http://" + host + ":" + httpPort + "/sparql";

        SparqlService httpSparqlService = FluentSparqlService.http(endpointUrl).create();

        
        Connection sqlConn;
        String jdbcUrl = "jdbc:virtuoso://" + host + ":" + odbcPort;
        try {
            sqlConn = DriverManager.getConnection(jdbcUrl, "dba", "dba");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to " + jdbcUrl, e);
        }
        
        SparqlQueryConnection queryConn = new SparqlQueryConnectionJsa(httpSparqlService.getQueryExecutionFactory());
        SparqlUpdateConnection updateConn = new SparqlUpdateConnectionJsa(httpSparqlService.getUpdateExecutionFactory());
        RDFDatasetConnection datasetConn = new RDFDatasetConnectionVirtuoso(queryConn, sqlConn);
        
        RDFConnection result = new RDFConnectionModular(queryConn, updateConn, datasetConn);

        return result;
    }
    
    /**
     * The default connection is some application specific connection. At
     * minimum it should enable health check queries.
     *
     * The default connection will typically refer to the service's openly accessible (HTTP) SPARQL endpoint.
     *
     *
     * @return
     * @throws InvalidFileFormatException
     * @throws IOException
     */
    public RDFConnection createDefaultConnection() {
        // Autoconfigure URLs based on the ini file
        int odbcPort = virtIniPrefs.node("Parameters").getInt("ServerPort", -1);
        int httpPort = virtIniPrefs.node("HTTPServer").getInt("ServerPort", -1);

        String host = "localhost";
//        String endpointUrl = "http://localhost:" + httpPort + "/sparql";
//
//        SparqlService httpSparqlService = FluentSparqlService.http(endpointUrl).create();
//
//        
//        Connection sqlConn;
//        String jdbcUrl = "jdbc:virtuoso://localhost:" + odbcPort;
//        try {
//            sqlConn = DriverManager.getConnection(jdbcUrl, "dba", "dba");
//        } catch (SQLException e) {
//            throw new RuntimeException("Failed to connect to " + jdbcUrl, e);
//        }
//        
//        SparqlQueryConnection queryConn = new SparqlQueryConnectionJsa(httpSparqlService.getQueryExecutionFactory());
//        SparqlUpdateConnection updateConn = new SparqlUpdateConnectionJsa(httpSparqlService.getUpdateExecutionFactory());
//        RDFDatasetConnection datasetConn = new RDFDatasetConnectionVirtuoso(queryConn, sqlConn);
        
//        RDFConnection result = new RDFConnectionModular(queryConn, updateConn, datasetConn);

        RDFConnection result = connectVirtuoso(host, httpPort, odbcPort);
        logger.debug("Automatically detected these ports in the virtuoso configuration:");
        logger.debug("ODBC port: " + odbcPort + " --- HTTP port: " + httpPort);

        return result;
    }

    public static void main(String[] args) throws Exception {
        VirtuosoSystemService virtService = new VirtuosoSystemService(
                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit_1112_8891/virtuoso.ini"));

        // CompletableFuture<Boolean> startupFuture = virtWrap.start();

        virtService.startAsync();
        logger.info("Waiting for startup..");
        try {
            virtService.awaitRunning(10, TimeUnit.SECONDS);
        } catch(Exception e) {
            logger.info("Failure", e);
            virtService.stopAsync();
            return;
        }
        logger.info("Startup complete");

        // Alternative callback style
        // startupFuture.thenAccept(status -> {
        // System.out.println("Service is healthy");
        // });

        virtService.createDefaultConnection();

        Thread.sleep(5000);
        virtService.stopAsync();
        System.err.println("Stopping...");
        virtService.awaitTerminated();
    }

}
