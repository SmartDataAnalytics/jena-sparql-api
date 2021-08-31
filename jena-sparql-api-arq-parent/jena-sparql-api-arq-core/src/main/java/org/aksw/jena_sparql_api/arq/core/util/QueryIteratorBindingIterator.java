package org.aksw.jena_sparql_api.arq.core.util;

import java.util.Iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.serializer.SerializationContext;

public class QueryIteratorBindingIterator extends QueryIteratorBase
{
    private Iterator<Binding> resultSet ;
    public QueryIteratorBindingIterator(Iterator<Binding> rs) { resultSet = rs ; }

    @Override
    protected void closeIterator()          { resultSet = null ; }
    @Override
    protected void requestCancel()          { }
    @Override
    protected boolean hasNextBinding()      { return resultSet.hasNext() ; }
    @Override
    protected Binding moveToNextBinding()   { return resultSet.next() ; }

    @Override
    public void output(IndentedWriter out, SerializationContext cxt)
    {
        out.print(Lib.className(this)) ;
    }
}