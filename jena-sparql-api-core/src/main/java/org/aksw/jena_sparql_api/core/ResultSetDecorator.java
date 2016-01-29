package org.aksw.jena_sparql_api.core;


import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 11:01 PM
 */
public class ResultSetDecorator
    implements ResultSet
{
    protected ResultSet decoratee;

    public ResultSetDecorator(ResultSet decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public boolean hasNext() {
        return decoratee.hasNext();
    }

    @Override
    public QuerySolution next() {
        return decoratee.next();
    }

    @Override
    public void remove() {
        decoratee.remove();
    }

    @Override
    public QuerySolution nextSolution() {
        return decoratee.nextSolution();
    }

    @Override
    public Binding nextBinding() {
        return decoratee.nextBinding();
    }

    @Override
    public int getRowNumber() {
        return decoratee.getRowNumber();
    }

    @Override
    public List<String> getResultVars() {
        return decoratee.getResultVars();
    }

    @Override
    public Model getResourceModel() {
        return decoratee.getResourceModel();
    }
}
