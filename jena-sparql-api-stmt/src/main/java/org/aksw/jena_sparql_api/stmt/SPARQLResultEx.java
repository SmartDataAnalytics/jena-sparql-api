package org.aksw.jena_sparql_api.stmt;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;

/**
 * Extension of SPARQLResult (inception Jena 3.8.0)
 * to add result types for iterators of triples / quads and update statements.
 * 
 * @author Claus Stadler, Nov 9, 2018
 *
 */
public class SPARQLResultEx
	extends SPARQLResult
	implements AutoCloseable
{	
	protected Iterator<Triple> triples;
	protected Iterator<Quad> quads;
	
	protected boolean updateType;

	protected Runnable closeAction = null;
	
	public SPARQLResultEx() {
		//super()
	}
	
    public SPARQLResultEx(Model model) {
        super(model);
    }

    public SPARQLResultEx(ResultSet resultSet, Runnable closeAction) {
        super(resultSet);
        this.closeAction = closeAction;
    }

    public SPARQLResultEx(boolean booleanResult) {
        super(booleanResult);
    }

    public SPARQLResultEx(Dataset dataset) {
        super(dataset);
    }

    public SPARQLResultEx(Iterator<JsonObject> jsonItems, Runnable closeAction) {
        super(jsonItems); 
        this.closeAction = closeAction;
    }
	
	public SPARQLResultEx(SPARQLResult that) {
		if(that.isBoolean()) { set(that.getBooleanResult()); }
		else if(that.isDataset()) { set(that.getDataset()); } 
		else if(that.isJson()) { set(that.getJsonItems()); }
		else if(that.isModel()) { set(that.getModel()); }
		else if(that.isResultSet()) { set(that.getResultSet()); }
		else { throw new IllegalArgumentException("Unknown SPARQLResult type"); }
	}

	public boolean isTriples() {
        if ( !isHasBeenSet() )
            throw new ResultSetException("Not set");
        return triples != null;
	}
	
	public Iterator<Triple> getTriples() {
        if ( !isHasBeenSet() )
            throw new ResultSetException("Not set");
        if ( !isTriples() )
            throw new ResultSetException("Not a Triples result");
        return triples;
	}
	
	public boolean isQuads() {
        if ( !isHasBeenSet() )
            throw new ResultSetException("Not set");
        return quads != null;
	}
	
	public Iterator<Quad> getQuads() {
        if ( !isHasBeenSet() )
            throw new ResultSetException("Not set");
        if ( !isQuads() )
            throw new ResultSetException("Not a Quads result");
        return quads;
	}
	
	
	public boolean isUpdateType() {
        if ( !isHasBeenSet() )
            throw new ResultSetException("Not set");
        return updateType;
	}
	
	public static SPARQLResult copy(SPARQLResult that) {
		SPARQLResult result =
			that.isBoolean() ? new SPARQLResult(that.getBooleanResult()) :
			that.isDataset() ? new SPARQLResult(that.getDataset()) :
			that.isJson() ? new SPARQLResult(that.getJsonItems()) :
			that.isModel() ? new SPARQLResult(that.getModel()) :
			that.isResultSet() ? new SPARQLResult(that.getResultSet()) : null;

		return result;
	}
	
	protected void setTriples(Iterator<Triple> triples, Runnable closeAction) {
		this.triples = triples;
		this.closeAction = closeAction;
		set((ResultSet)null);
	}

	protected void setQuads(Iterator<Quad> quads, Runnable closeAction) {
		this.quads = quads;
		this.closeAction = closeAction;
		set((ResultSet)null);
	}
	
	protected void setUpdateType() {
		this.updateType = true;
		set((ResultSet)null);
	}


	public static SPARQLResultEx createTriples(Iterator<Triple> triples, Runnable closeAction) {
		SPARQLResultEx result = new SPARQLResultEx(); 
		result.setTriples(triples, closeAction);
		return result;
	}

	public static SPARQLResultEx createQuads(Iterator<Quad> quads, Runnable closeAction) {
		SPARQLResultEx result = new SPARQLResultEx(); 
		result.setQuads(quads, closeAction);
		return result;
	}

	public static SPARQLResultEx createUpdateType() {
		SPARQLResultEx result = new SPARQLResultEx(); 
		result.setUpdateType();
		return result;
	}

	@Override
	public void close() throws Exception {
		if(closeAction != null) {
			closeAction.run();
		}
	}
}
