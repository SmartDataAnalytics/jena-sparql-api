package org.aksw.jena_sparql_api.lookup;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;

public class FunctionCloneResultSet
    implements Function<ResultSet, ResultSet>
{
    @Override
    public ResultSet apply(ResultSet input) {
        ResultSetMem tmp = (ResultSetMem)input;

        //ResultSet result = ResultSetFactory.makeRewindable(input);
        ResultSetMem result = new ResultSetMem(tmp);
        
        tmp.rewind();
        return result;
    }
    
    public static final FunctionCloneResultSet fn = new FunctionCloneResultSet();
}
