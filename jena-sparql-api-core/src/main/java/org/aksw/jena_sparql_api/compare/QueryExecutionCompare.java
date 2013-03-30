package org.aksw.jena_sparql_api.compare;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.QuerySolutionBase;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.ResultSetUtils;
import com.hp.hpl.jena.sparql.util.VarUtils;
import com.hp.hpl.jena.util.FileManager;
import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.collections.diff.ListDiff;
import org.aksw.commons.collections.diff.ModelDiff;
import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.validator.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 1/5/12
 *         Time: 12:33 AM
 */
public class QueryExecutionCompare
    implements QueryExecution
{

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCompare.class);


    public static Multiset<QuerySolution> toMultiset(ResultSet rs) {
        Multiset<QuerySolution> result = HashMultiset.create();
        while(rs.hasNext()) {
            QuerySolution original = rs.next();

            QuerySolution wrapped = new QuerySolutionWithEquals(original);

            result.add(wrapped);
        }

        return result;
    }

    /**
     * Traverse the resultset in order, and write out the missing items on each side:
     * 1 2
     * ---
     * a a
     * b c
     * d d
     *
     * gives:
     * [c] [b]
     *
     * (1 lacks c, 2 lacks b)
     *
     *
     * @param a
     * @param b
     * @return
     */
    public static ListDiff<QuerySolution> compareOrdered(ResultSet a, ResultSet b) {
        ListDiff<QuerySolution> result = new ListDiff<QuerySolution>();

        QuerySolution x = null;
        QuerySolution y = null;

        while(a.hasNext()) {
            if(!b.hasNext()) {
                while(a.hasNext()) {
                    result.getAdded().add(a.next());
                }
                return result;
            }

            //if((x == null && y == null) ||  x.equals(y)
            if(x == y || x.equals(y)) {
                x = a.next();
                y = b.next();
                continue;
            }

            String sx = x.toString();
            String sy = y.toString();

            if(sx.compareTo(sy) < 0) {
                result.getRemoved().add(x);
                x = a.next();
            } else {
                result.getAdded().add(y);
                y = b.next();
            }
        }

        while(b.hasNext()) {
            result.getRemoved().add(b.next());
        }

        return result;
    }

    public static ListDiff<QuerySolution> compareUnordered(ResultSet a, ResultSet b) {
        ListDiff<QuerySolution> result = new ListDiff<QuerySolution>();

        Multiset<QuerySolution> x = toMultiset(a);
        Multiset<QuerySolution> y = toMultiset(b);

        Multiset<QuerySolution> common = HashMultiset.create(Multisets.intersection(x, y));

        y.removeAll(common);
        x.removeAll(common);

        result.getAdded().addAll(y);
        result.getRemoved().addAll(x);

        return result;
    }

    public static ModelDiff compareModel(Model a, Model b) {
        ModelDiff result = new ModelDiff();

        result.getAdded().add(b);
        result.getAdded().remove(a);

        result.getRemoved().add(a);
        result.getRemoved().remove(b);

        return result;
    }

    private boolean isOrdered; // Whether the result sets are ordered
    private QueryExecution a;
    private QueryExecution b;

    private Query query = null;

    private ListDiff<QuerySolution> resultSetDiff = null; // The diff after the query execution
    private ModelDiff modelDiff = null;
    private Diff<Boolean> askDiff = null;


    public boolean isDifference() {
        if(resultSetDiff != null) {
            return !(resultSetDiff.getAdded().isEmpty() && resultSetDiff.getRemoved().isEmpty());
        } else if(modelDiff != null) {
            return !(modelDiff.getAdded().isEmpty() && modelDiff.getRemoved().isEmpty());
        } else if(askDiff != null) {
            return !(askDiff.getAdded() == askDiff.getRemoved());
        } else {
            throw new RuntimeException("Cannot retrieve difference because query was not executed.");
        }
    }

    public QueryExecutionCompare(Query query, QueryExecution a, QueryExecution b, boolean isOrdered) {
        this.query = query;
        this.a = a;
        this.b = b;
        this.isOrdered = isOrdered;
    }


    /**
     * Set the FileManger that might be used to load files.
     * May not be supported by all QueryExecution implementations.
     */
    @Override
    public void setFileManager(FileManager fm) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Set the initial association of variables and values.
     * May not be supported by all QueryExecution implementations.
     *
     * @param binding
     */
    @Override
    public void setInitialBinding(QuerySolution binding) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The dataset against which the query will execute.
     * May be null, implying it is expected that the query itself
     * has a dataset description.
     */
    @Override
    public Dataset getDataset() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The properties associated with a query execution -
     * implementation specific parameters  This includes
     * Java objects (so it is not an RDF graph).
     * Keys should be URIs as strings.
     * May be null (this implementation does not provide any configuration).
     */
    @Override
    public Context getContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * The query associated with a query execution.
     * May be null (QueryExecution may have been created by other means)
     */
    @Override
    public Query getQuery() {
        return query;
    }

    /**
     * Execute a SELECT query
     */
    @Override
    public ResultSet execSelect() {
        ResultSetRewindable x;
        ResultSet y;
        try {
             x = ResultSetFactory.makeRewindable(a.execSelect());
             y = b.execSelect();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            resultSetDiff = new ListDiff<QuerySolution>();
            throw e;
        }

        resultSetDiff = (isOrdered)
                ? compareOrdered(x, y)
                : compareUnordered(x, y);

        x.reset();

        logResultSet();

        return x;
    }

    public void log(long added, long removed) {
        String msg = added + "\t" + removed + "\t" + StringUtils.urlEncode("" + query);

        if(added == 0 && removed == 0) {
            logger.info("[ OK ] " + msg);
        } else {
            logger.warn("[FAIL] " + msg);
        }
    }

    public void logResultSet() {
        log(resultSetDiff.getAdded().size(), resultSetDiff.getRemoved().size());
    }

    public void logModel() {
        log(modelDiff.getAdded().size(), modelDiff.getRemoved().size());
    }

    public void logAsk() {
        boolean  added = askDiff.getAdded();
        boolean  removed = askDiff.getRemoved();

        String msg = added + "\t" + removed + "\t" + StringUtils.urlEncode("" + query);

        if(added == removed) {
            logger.trace("[ OK ] " + msg);
        } else {
            logger.warn("[FAIL] " + msg);
        }
    }


    /**
     * Execute a CONSTRUCT query
     */
    @Override
    public Model execConstruct() {
        return execConstruct(ModelFactory.createDefaultModel());
    }

    /**
     * Execute a CONSTRUCT query, putting the statements into 'model'.
     *
     * @return Model The model argument for casaded code.
     */
    @Override
    public Model execConstruct(Model model) {
        Model x;
        Model y;
        try {
             x = a.execConstruct();
             y = b.execConstruct();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            modelDiff = new ModelDiff();
            throw e;
        }

        modelDiff = compareModel(x, y);

        logModel();

        return x;
    }

    /**
     * Execute a DESCRIBE query
     */
    @Override
    public Model execDescribe() {
        return execDescribe(ModelFactory.createDefaultModel());
    }

    /**
     * Execute a DESCRIBE query, putting the statements into 'model'.
     *
     * @return Model The model argument for casaded code.
     */
    @Override
    public Model execDescribe(Model model) {
        Model x;
        Model y;
        try {
             x = a.execDescribe();
             y = b.execDescribe();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            modelDiff = new ModelDiff();
            throw e;
        }

        modelDiff = compareModel(x, y);

        logModel();

        return x;
    }

    /**
     * Execute an ASK query
     */
    @Override
    public boolean execAsk() {
        boolean x;
        boolean y;

        try {
             x = a.execAsk();
             y = b.execAsk();
        } catch(RuntimeException e) {
            // Set diff in order to indicate that the execution was performed
            askDiff = new Diff<Boolean>(false, false, null);
            throw e;
        }

        askDiff = new Diff<Boolean>(x, y, null);

        logAsk();

        return x;
    }

    /**
     * Stop in mid execution.
     * This method can be called in parallel with other methods on the
     * QueryExecution object.
     * There is no guarantee that the concrete implementation actual
     * will stop or that it will do so immediately.
     * No operations on the query execution or any associated
     * result set are permitted after this call and may cause exceptions to be thrown.
     */
    @Override
    public void abort() {
        try {
            a.abort();
        } finally {
            b.abort();
        }
    }

    /**
     * Close the query execution and stop query evaluation as soon as convenient.
     * It is important to close query execution objects in order to release
     * resources such as working memory and to stop the query execution.
     * Some storage subsystems require explicit ends of operations and this
     * operation will cause those to be called where necessary.
     * No operations on the query execution or any associated
     * result set are permitted after this call.
     * This method should not be called in parallel with other methods on the
     * QueryExecution object.
     */
    @Override
    public void close() {
        try {
            a.close();
        } finally {
            b.close();
        }
    }

    /**
     * Set a timeout on the query execution.
     * Processing will be aborted after the timeout (which starts when the approprate exec call is made).
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout.
     */
    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        a.setTimeout(timeout, timeoutUnits);
        b.setTimeout(timeout, timeoutUnits);
    }

    /**
     * Set time, in milliseconds
     *
     * @see #setTimeout(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void setTimeout(long timeout) {
        a.setTimeout(timeout);
        b.setTimeout(timeout);
    }

    /**
     * Set timeouts on the query execution; the first timeout refers to time to first result,
     * the second refers to overall query execution after the first result.
     * Processing will be aborted if a timeout expires.
     * Not all query execution systems support timeouts.
     * A timeout of less than zero means no timeout; this can be used for timeout1 or timeout2.
     */
    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        a.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
        b.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    /**
     * Set time, in milliseconds
     *
     * @see #setTimeout(long, java.util.concurrent.TimeUnit, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void setTimeout(long timeout1, long timeout2) {
        a.setTimeout(timeout1, timeout2);
        b.setTimeout(timeout1, timeout2);
    }

    /*
    @Override
    public Iterator<Triple> execConstructStreaming() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<Triple> execDescribeStreaming() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }*/
}
