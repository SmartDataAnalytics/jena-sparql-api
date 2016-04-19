package org.aksw.jena_sparql_api.delay.core;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11 Time: 10:57 AM
 */
public class QueryExecutionDelay extends QueryExecutionDecorator
{
    private static final Logger logger = LoggerFactory
            .getLogger(QueryExecutionDelay.class);

    private Delayer delayer;

    public QueryExecutionDelay(QueryExecution decoratee, Delayer delayer) {
        super(decoratee);
        this.delayer = delayer;
    }

    protected void doDelay() {
        try {
            delayer.doDelay();
        } catch(InterruptedException e) {
            logger.warn("Interrupted: ", e);
        }
    }

    @Override
    public ResultSet execSelect() {
        doDelay();
        return super.execSelect();
    }

    @Override
    public Model execConstruct() {
        doDelay();
        return super.execConstruct();
    }

    @Override
    public Model execConstruct(Model model) {
        doDelay();
        return super.execConstruct(model);
    }

    @Override
    public Model execDescribe() {
        doDelay();
        return super.execDescribe();
    }

    @Override
    public Model execDescribe(Model model) {
        doDelay();
        return super.execDescribe(model);
    }

    @Override
    public boolean execAsk() {
        doDelay();
        return super.execAsk();
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        doDelay();
        return super.execConstructTriples();
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        doDelay();
        return super.execConstructTriples();
    }

}
