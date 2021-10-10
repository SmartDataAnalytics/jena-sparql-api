package org.aksw.jena_sparql_api.rx.util.connection;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.utils.Symbols;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class RDFConnectionUtils {

    public static SparqlQueryConnection unwrapQueryConnection(SparqlQueryConnection conn) {
        SparqlQueryConnection result;
        if(conn instanceof RDFConnectionModular) {
            SparqlQueryConnection tmp = getQueryConnection((RDFConnectionModular)conn);
            result = unwrapQueryConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }

    public static SparqlUpdateConnection unwrapUpdateConnection(SparqlUpdateConnection conn) {
        SparqlUpdateConnection result;
        if(conn instanceof RDFConnectionModular) {
            SparqlUpdateConnection tmp = getUpdateConnection((RDFConnectionModular)conn);
            result = unwrapUpdateConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }

    public static RDFDatasetConnection unwrapDatasetConnection(RDFDatasetConnection conn) {
        RDFDatasetConnection result;
        if(conn instanceof RDFConnectionModular) {
            RDFDatasetConnection tmp = getDatasetConnection((RDFConnectionModular)conn);
            result = unwrapDatasetConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }


    /** Reflective access to an {@link RDFConnectionModular}'s queryConnection. */
    public static SparqlQueryConnection getQueryConnection(RDFConnectionModular conn) {
        SparqlQueryConnection result;
        try {
            Field f = RDFConnectionModular.class.getDeclaredField("queryConnection");
            f.setAccessible(true);
            result = (SparqlQueryConnection)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFConnectionModular}'s updateConnection. */
    public static SparqlUpdateConnection getUpdateConnection(RDFConnectionModular conn) {
        SparqlUpdateConnection result;
        try {
            Field f = RDFConnectionModular.class.getDeclaredField("updateConnection");
            f.setAccessible(true);
            result = (SparqlUpdateConnection)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFConnectionModular}'s datasetConnection. */
    public static RDFDatasetConnection getDatasetConnection(RDFConnectionModular conn) {
        RDFDatasetConnection result;
        try {
            Field f = RDFConnectionModular.class.getDeclaredField("datasetConnection");
            f.setAccessible(true);
            result = (RDFDatasetConnection)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFConnectionModular}'s dataset. */
    public static Dataset getDataset(RDFConnectionLocal conn) {
        Dataset result;
        try {
            Field f = RDFConnectionLocal.class.getDeclaredField("dataset");
            f.setAccessible(true);
            result = (Dataset)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }



    public static RDFConnection wrapWithContextMutator(RDFConnection rawConn) {
        return wrapWithContextMutator(rawConn, cxt -> {});
    }



    /**
     * Places the connection object as a symbol into to context,
     * so that custom functions - notably E_Benchmark can
     * pose further queries to it.
     *
     * FIXME Connections are usually not intended for concurrent use;
     * we should put a connection supplier into the context instead!
     *
     * @param rawConn
     * @return
     */
    // Ideally replace with wrapWithPostProcessor
    // ISSUE: With the connection interface we cannot mutate the context of update requests
    // Hence the existance of this method is still justified
    public static RDFConnection wrapWithContextMutator(RDFConnection rawConn, Consumer<Context> contextMutator) {
        RDFConnection[] result = {null};

        SparqlUpdateConnection tmp = unwrapUpdateConnection(rawConn);
        Dataset dataset = tmp instanceof RDFConnectionLocal
                ? getDataset((RDFConnectionLocal)tmp)
                : null;

        result[0] =
            new RDFConnectionModular(rawConn, rawConn, rawConn) {
                public QueryExecution query(Query query) {
                    return postProcess(rawConn.query(query));
                }

                @Override
                public QueryExecution query(String queryString) {
                    return postProcess(rawConn.query(queryString));
                }


                @Override
                public void update(UpdateRequest update) {
//			        checkOpen();
//			        Txn.executeWrite(dataset, () -> {
                        UpdateProcessor tmp = UpdateExecutionFactory.create(update, dataset);
                        UpdateProcessor up = postProcess(tmp);
                        up.execute();
//			        });
                }


                public UpdateProcessor postProcess(UpdateProcessor qe) {
                    Context cxt = qe.getContext();
                    if(cxt != null) {
                        cxt.set(Symbols.symConnection, result[0]);
                        contextMutator.accept(cxt);
                    }

                    return qe;
                }

                public QueryExecution postProcess(QueryExecution qe) {
                    Context cxt = qe.getContext();
                    if(cxt != null) {
                        cxt.set(Symbols.symConnection, result[0]);
                        contextMutator.accept(cxt);
                    }

                    return qe;
                }
            };

        return result[0];
    }

}
