package org.aksw.jena_sparql_api.concept_cache.trash;

import org.aksw.jena_sparql_api.concept_cache.dirty.OpCache;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCaching;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * Operator to perform caching during execution
 *
 * @author raven
 *
 */
public class OpSubQueryExecution
    extends OpExt
{
    protected QueryExecutionFactory qef;
    protected Query query;

    public OpSubQueryExecution(QueryExecutionFactory qef, Query query) {
        super(OpSubQueryExecution.class.getSimpleName());

        this.qef = qef;
        this.query = query;
    }

    @Override
    public Op effectiveOp() {
        return null;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        QueryIterator result = new QueryIteratorResultSet(rs);
        return result;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        out.println("OpSubQueryExecution[" + query + "@" + qef + "]");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qef == null) ? 0 : qef.hashCode());
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        return result;
    }

    @Override
    public boolean equalTo(Op obj, NodeIsomorphismMap labelMap) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpSubQueryExecution other = (OpSubQueryExecution) obj;
        if (qef == null) {
            if (other.qef != null)
                return false;
        } else if (!qef.equals(other.qef))
            return false;
        if (query == null) {
            if (other.query != null)
                return false;
        } else if (!query.equals(other.query))
            return false;
        return true;
    }
}
