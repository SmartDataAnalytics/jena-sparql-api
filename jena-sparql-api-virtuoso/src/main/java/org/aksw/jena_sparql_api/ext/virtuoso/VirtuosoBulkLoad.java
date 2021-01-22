package org.aksw.jena_sparql_api.ext.virtuoso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VirtuosoBulkLoad {


    private static final Logger logger = LoggerFactory.getLogger(VirtuosoBulkLoad.class);

    public static void logEnable(Connection conn, int bits, int quiet) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("log_enable(?, ?)")) {
            stmt.setInt(1, bits);
            stmt.setInt(2, quiet);
            logger.debug("Setting log_enable(" + bits + ", " + quiet +")");
            stmt.execute();
        }
    }

    public static void dropGraph(Connection conn, String targetGraphIri, boolean silent) throws SQLException {
        executeGraphOp(conn, "drop", targetGraphIri, silent);
    }

    public static void clearGraph(Connection conn, String targetGraphIri, boolean silent) throws SQLException {
        executeGraphOp(conn, "clear", targetGraphIri, silent);
    }

    public static void executeGraphOp(Connection conn, String opName, String targetGraphIri, boolean silent) throws SQLException {
        try(Statement stmt = conn.createStatement()) {
            //stmt.setString(1, "<" + targetGraphIri + "");
            logger.debug("Executing " + opName + " graph with arguments " + targetGraphIri + ", " + silent);
            stmt.execute("sparql " + opName + " "  + (silent ? " silent " : "") + "graph <" + targetGraphIri + ">");
            logger.debug("Done executing " + opName + " graph");
        }
    }
    public static void ldDir(Connection conn, String dir, String fileNamePattern, String targetGraphIri) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("ld_dir(?, ?, ?)")) {
            stmt.setString(1, dir);
            stmt.setString(2, fileNamePattern);
            stmt.setString(3, targetGraphIri);
            logger.debug("Executing ld_dir(" + dir + ", " + fileNamePattern + ", " + targetGraphIri + ")");
            stmt.execute();
            logger.debug("Done executing ld_dir()");
        }
    }

    public static void rdfLoaderRun(Connection conn) throws SQLException {
         try(Statement stmt = conn.createStatement()) {
             logger.debug("Executing rdf_loader_run()");
             stmt.execute("rdf_loader_run()");
             logger.debug("Done executing rdf_loader_run()");
         }
    }

    public static void checkpoint(Connection conn) throws SQLException {
        try(Statement stmt = conn.createStatement()) {
            stmt.execute("checkpoint");
        }
    }

    public static void load(Connection conn, File file, String targetGraphIri) throws SQLException {

        logger.debug("Loading file: " + file.getAbsolutePath());
        File parentFile = file.getParentFile();
        String dir = parentFile.getAbsolutePath();
        String filename = file.getName();
        ldDir(conn, dir, filename, targetGraphIri);
        rdfLoaderRun(conn);
        logger.debug("Done loading file: " + file.getAbsolutePath());
    }

    public static void streamToFile(Stream<Triple> triples, int batchSize, File file) throws FileNotFoundException, IOException {
        try(OutputStream out = new FileOutputStream(file)) {
            logger.debug("Serializing stream to file: " + file.getAbsolutePath());

            StreamUtils.mapToBatch(triples, batchSize)
                .forEach(batch -> {
                    Graph graph = GraphFactory.createGraphMem();
                    batch.forEach(graph::add);
                    RDFDataMgr.write(out, graph, Lang.NTRIPLES);
                });

            out.flush();
            logger.debug("Done writing to file: " + file.getAbsolutePath());
        }
    }

    public static void load(Connection conn, Stream<Triple> triples, String targetGraphIri, int batchSize) throws IOException, SQLException {
        File file = File.createTempFile("virt-bulk-load-", ".nt");
        file.deleteOnExit();

        //Thread thread = new Thread(() -> file.delete());
        try {
            //Runtime.getRuntime().addShutdownHook(thread);

            streamToFile(triples, batchSize, file);
            load(conn, file, targetGraphIri);

        } finally {
            //Runtime.getRuntime().removeShutdownHook(thread);
            //thread.run();
            file.delete();
        }
    }



    public static void graphGroupCreate(Connection conn, String iri, int quiet) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("DB.DBA.RDF_GRAPH_GROUP_CREATE(?, ?)")) {
            stmt.setString(1, iri);
            stmt.setInt(2, quiet);
            //logger.debug("Executing ld_dir(" + dir + ", " + fileNamePattern + ", " + targetGraphIri + ")");
            stmt.execute();
            //logger.debug("Done executing ld_dir()");
        }
    }

    public static void graphGroupDrop(Connection conn, String iri, int quiet) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("DB.DBA.RDF_GRAPH_GROUP_DROP(?, ?)")) {
            stmt.setString(1, iri);
            stmt.setInt(2, quiet);
            //logger.debug("Executing ld_dir(" + dir + ", " + fileNamePattern + ", " + targetGraphIri + ")");
            stmt.execute();
            //logger.debug("Done executing ld_dir()");
        }
    }

    public static void graphGroupIns(Connection conn, String group, String member) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("DB.DBA.RDF_GRAPH_GROUP_INS(?, ?)")) {
            stmt.setString(1, group);
            stmt.setString(2, member);
            //logger.debug("Executing ld_dir(" + dir + ", " + fileNamePattern + ", " + targetGraphIri + ")");
            stmt.execute();
            //logger.debug("Done executing ld_dir()");
        }
    }

    public static void graphGroupDel(Connection conn, String group, String member) throws SQLException {
        try(PreparedStatement stmt = conn.prepareStatement("DB.DBA.RDF_GRAPH_GROUP_DEL(?, ?)")) {
            stmt.setString(1, group);
            stmt.setString(2, member);
            //logger.debug("Executing ld_dir(" + dir + ", " + fileNamePattern + ", " + targetGraphIri + ")");
            stmt.execute();
            //logger.debug("Done executing ld_dir()");
        }
    }

/*
    public static void main(String[] args) throws SQLException, MalformedURLException, IOException {
        //Class.forName("");
        String url = "jdbc:virtuoso://localhost:1112";
        String targetGraphIri = "http://mygraph.org/";


        try(Connection conn = DriverManager.getConnection(url, "dba", "dba")) {
            logEnable(conn, 2, 0);
            dropGraph(conn, targetGraphIri, true);
            checkpoint(conn);

            try(InputStream in = new BZip2CompressorInputStream(new URL("http://downloads.linkedgeodata.org/releases/2015-11-02/2015-11-02-Abutters.way.sorted.nt.bz2").openStream(), true)) {
                Stream<Triple> stream = Streams.stream(RDFDataMgr.createIteratorTriples(in, Lang.NTRIPLES, "http://example.org/"));

                load(conn, stream, "http://mygraph.org/", 10000);
                checkpoint(conn);
            }
        }
    }
*/
}
