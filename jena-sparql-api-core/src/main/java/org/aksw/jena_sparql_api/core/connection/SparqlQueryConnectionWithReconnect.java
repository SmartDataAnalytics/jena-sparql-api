package org.aksw.jena_sparql_api.core.connection;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.commons.util.healthcheck.HealthcheckRunner;
import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection wrapper that tries to recover from loss of the underlying connection.
 * Neither replays transactions nor individual queries.
 *
 * If a query fails due to a connection loss then attempts ares made to establish a new connection.
 * If an attempt is successful then a ConnectionReestablishedException is raised which indicates that the
 * connection would be ready to accept workloads again.
 *
 * The main benefit of this class that it removes the need of passing a {@link java.sql.DataSource}-like object to
 * client methods that operate on a single connection. It also takes care of the resource management such
 * as closing the connections used for probing.
 * The client methods can catch the
 * ConnectionReestablishedException and act accordingly (such as by replaying some queries or moving on
 * the the next workload).
 *
 * @author raven
 *
 */
public class SparqlQueryConnectionWithReconnect
    extends TransactionalDelegate
    implements SparqlQueryConnectionTmp
{
    private static final Logger logger = LoggerFactory.getLogger(SparqlQueryConnectionWithReconnect.class);

    protected Callable<SparqlQueryConnection> dataConnectionSupplier;
    protected Callable<SparqlQueryConnection> probeConnectionSupplier;

    protected Query healthCheckQuery = QueryFactory.create("SELECT * { ?s <http://www.example.org/rdf/type> <http://www.example.org/rdf/Resource> }");
    protected Supplier<HealthcheckRunner.Builder> healthCheckBuilder;

    protected boolean isLost = false;

    public SparqlQueryConnectionWithReconnect(
            Callable<SparqlQueryConnection> dataConnectionSupplier,
            Callable<SparqlQueryConnection> probeConnectionSupplier,
            Supplier<HealthcheckRunner.Builder> healthCheckBuilder,
            SparqlQueryConnection activeDelegate) {
        super();
        this.dataConnectionSupplier = dataConnectionSupplier;
        this.probeConnectionSupplier = probeConnectionSupplier;
        this.activeDelegate = activeDelegate;
        this.healthCheckBuilder = healthCheckBuilder;
    }


    /** Immediately obtain a connection from the supplier */
    public static SparqlQueryConnectionWithReconnect create(
            Callable<SparqlQueryConnection> connectionSupplier
            ) throws Exception {
        SparqlQueryConnection conn = connectionSupplier.call();

        return new SparqlQueryConnectionWithReconnect(
                connectionSupplier,
                connectionSupplier,
                () -> HealthcheckRunner
                    .builder().setRetryCount(Long.MAX_VALUE).setInterval(5, TimeUnit.SECONDS),
                conn);
    }

    /** The currently active connection */
    protected SparqlQueryConnection activeDelegate;

    @Override
    protected Transactional getDelegate() {
        return activeDelegate;
    }

    @Override
    public QueryExecution query(Query query) {
        if (isLost) {
            throw new ConnectionLostException("conneection lost");
        }

        QueryExecution core = activeDelegate.query(query);
        QueryExecution wrapped = new QueryExecutionWithReconnect(core);
        return wrapped;
    }


    protected boolean isConnectionProblemException(Throwable t) {
        return ExceptionUtilsAksw.isConnectionRefusedException(t)
                || ExceptionUtilsAksw.isUnknownHostException(t);
    }


    protected void forceCloseActiveConn() {
        try {
            if (activeDelegate != null) {
                activeDelegate.close();
            }
        } catch (Exception e) {
            logger.warn("Exception while attempting to close an apparently lost connecetion", e);
        }
        activeDelegate = null;
    }

    /**
     * This method is run by the healthcheck runner until there is no more exception
     *
     * @throws Exception
     */
    protected synchronized void tryRecovery() throws Exception {
        forceCloseActiveConn();

        boolean reuseProbeConn = probeConnectionSupplier == dataConnectionSupplier;

        SparqlQueryConnection probeConn = null;
        try {
            probeConn = probeConnectionSupplier.call();
            try (QueryExecution qe = probeConn.query(healthCheckQuery)) {
                ResultSet rs = qe.execSelect();
                ResultSetFormatter.consume(rs);
            }
        } catch (Exception e) {
            if (probeConn != null) {
                probeConn.close();
            }
            throw new RuntimeException(e);
        }

        activeDelegate = reuseProbeConn
                ? probeConn
                : dataConnectionSupplier.call();
    }

    @Override
    public void close() {
        activeDelegate.close();
    }


    protected void testForConnectionProblem(Exception e) {
        if (isConnectionProblemException(e)) {
            handleConnectionProblem(e);
        }
        else {
            // Assume a 'normal' query exception
            throw new RuntimeException(e);
        }
    }

    protected void handleConnectionProblem(Exception e) {
        try {
            healthCheckBuilder.get()
                .setAction(() -> tryRecovery())
                .addFatalCondition(ex -> !isConnectionProblemException(ex))
                .build()
                .run();
        } catch (Exception mostRecentHealthCheckException) {
            isLost = true;
            throw new ConnectionLostException("connection lost", mostRecentHealthCheckException);
        }

        throw new ConnectionReestablishedException("connection re-established", e);
    }


    public class QueryExecutionWithReconnect
        extends QueryExecutionDecorator
    {
        public QueryExecutionWithReconnect(QueryExecution decoratee) {
            super(decoratee);
        }

        @Override
        protected void beforeExec() {
            super.beforeExec();

            if (isLost) {
                throw new ConnectionLostException("connection lost");
            }
        }

        @Override
        protected void onException(Exception e) {
            testForConnectionProblem(e);
        }
    }

}
