package org.aksw.jena_sparql_api.delay.core;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.aksw.jena_sparql_api.core.QueryExecutionStreamingDecorator;
import org.aksw.jena_sparql_api.delay.extra.Delayer;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:57 AM
 */
public class QueryExecutionDelay
    extends QueryExecutionStreamingDecorator

{
    private Delayer delayer;

    public QueryExecutionDelay(QueryExecutionStreaming decoratee, Delayer delayer) {
        super(decoratee);
        this.delayer = delayer;
    }

    @Override
     public ResultSet execSelect() {
        delayer.doDelay();
        return super.execSelect();
     }

     @Override
     public Model execConstruct() {
         delayer.doDelay();
         return super.execConstruct();
     }

     @Override
     public Model execConstruct(Model model) {
         delayer.doDelay();
         return super.execConstruct(model);
     }

     @Override
     public Model execDescribe() {
         delayer.doDelay();
         return super.execDescribe();
     }

     @Override
     public Model execDescribe(Model model) {
         delayer.doDelay();
         return super.execDescribe(model);
     }

     @Override
     public boolean execAsk() {
         delayer.doDelay();
         return super.execAsk();
     }
     
     @Override
     public Iterator<Triple> execConstructStreaming() {
         delayer.doDelay();
         return super.execConstructStreaming();
     }

     @Override
     public Iterator<Triple> execDescribeStreaming() {
         delayer.doDelay();
         return super.execConstructStreaming();
     }
     
}
