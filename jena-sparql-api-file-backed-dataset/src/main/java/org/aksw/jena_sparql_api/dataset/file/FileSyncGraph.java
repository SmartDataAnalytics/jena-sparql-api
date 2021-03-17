package org.aksw.jena_sparql_api.dataset.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.model.DatasetGraphDiff;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class for syncing a dataset graph with the content of a file
 *
 * @author raven
 *
 */
public class FileSyncGraph
    extends FileSyncBase {

    private static final Logger logger = LoggerFactory.getLogger(FileSyncGraph.class);

    protected DatasetGraph dsg;
    protected Path path;
    protected RDFFormat rdfFormat;
    protected Supplier<?> getVersion;

    public FileSyncGraph(
            Path path,
            RDFFormat rdfFormat,
            LockPolicy lockPolicy,
            Supplier<?> getVersion) throws Exception {
        this(DatasetGraphFactory.createTxnMem(), path, rdfFormat, lockPolicy, getVersion);
    }

    public FileSyncGraph(
            DatasetGraph dsg,
            Path path,
            RDFFormat rdfFormat,
            LockPolicy lockPolicy,
            Supplier<?> getVersion) throws Exception {
        super(lockPolicy, path);
        this.dsg = dsg;
        this.path = path;
        this.rdfFormat = rdfFormat;
        this.getVersion = getVersion;
    }

    @Override
    protected void deserializeFrom(FileChannel localFc) {
        logger.info("Loading data from " + path);
//      if(!dsg.isInTransaction()) {
//          throw new RuntimeException("we should be in a transaction here");
//      }


        // The input stream is intentionally not closed;
        // as it would close the file cannel.
        // The locks depend on the file channel, so the channel
        // needs to remain open for the time of transaction
        Lang lang = rdfFormat.getLang();
        InputStream in = new CloseShieldInputStream(Channels.newInputStream(localFc));
        Txn.executeWrite(dsg, () -> {
            dsg.clear();
            RDFDataMgr.read(dsg, in, lang);

            // Materialize the dataset graph so that loading
            // leaves the addition/removal sets empty
            if (dsg instanceof DatasetGraphDiff) {
                ((DatasetGraphDiff)dsg).materialize();
            }
        });

//        int hash = dsg.hashCode();
//        System.out.println("HASH: " + hash);
    }

    @Override
    protected void serializeTo(FileChannel localFc) {
        // FIXME Do not rewrite if there was no change!

//        int hash = dsg.hashCode();
//        System.out.println("HASH: " + hash);
        logger.info("Writing data to " + path);
        OutputStream out = new CloseShieldOutputStream(Channels.newOutputStream(localFc));
        RDFDataMgr.write(out, dsg, rdfFormat);
    }

    @Override
    protected Object getLoadedObjectVersion() {
        Object result = getVersion.get();
        return result;
        // return Streams.stream(dsg.find()).collect(Collectors.toList()).hashCode();
    }
};

