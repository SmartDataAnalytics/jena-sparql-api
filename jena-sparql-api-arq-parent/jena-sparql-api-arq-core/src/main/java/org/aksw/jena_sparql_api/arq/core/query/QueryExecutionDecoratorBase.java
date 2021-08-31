package org.aksw.jena_sparql_api.arq.core.query;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;


/**
 * Adds beforeExec and afterExec methods that can be used
 * to allocate and release resources upon performing an execution.
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:28 AM
 */
public class QueryExecutionDecoratorBase<T extends QueryExecution>
    implements QueryExecutionDecorator
{
    protected T decoratee;

    public QueryExecutionDecoratorBase(T decoratee) {
        super();
        this.decoratee = decoratee;
    }

    @Override
    public T getDelegate() {
        return decoratee;
    }

    protected void beforeExec() {

    }

    protected void afterExec() {

    }

    protected void onException(Exception e) {
    }

    @Override
    public ResultSet execSelect() {
        beforeExec();
        try {
            return decoratee.execSelect();
        } catch(Exception e) {
            onException(e);
            throw e;
//        	throw new RuntimeException(e);
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execConstruct() {
        beforeExec();
        try {
            return decoratee.execConstruct();
        } catch(Exception e) {
            onException(e);
            //throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execConstruct(Model model) {
        beforeExec();
        try {
            return decoratee.execConstruct(model);
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execDescribe() {
        beforeExec();
        try {
            return decoratee.execDescribe();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Model execDescribe(Model model) {
        beforeExec();
        try {
            return decoratee.execDescribe(model);
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public boolean execAsk() {
        beforeExec();
        try {
            return decoratee.execAsk();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        beforeExec();
        try {
            return decoratee.execConstructTriples();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        beforeExec();
        try {
            return decoratee.execDescribeTriples();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<Quad> execConstructQuads() {
        beforeExec();
        try {
            return decoratee.execConstructQuads();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Dataset execConstructDataset() {
        beforeExec();
        try {
            return decoratee.execConstructDataset();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Dataset execConstructDataset(Dataset dataset) {
        beforeExec();
        try {
            return decoratee.execConstructDataset(dataset);
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public JsonArray execJson() {
        beforeExec();
        try {
            return decoratee.execJson();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        beforeExec();
        try {
            return decoratee.execJsonItems();
        } catch(Exception e) {
            onException(e);
//        	throw new RuntimeException(e);
            throw e;
        } finally {
            afterExec();
        }
    }

}
