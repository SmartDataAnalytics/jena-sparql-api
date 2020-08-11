package org.aksw.jena_sparql_api.mapper;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.VarUtils;

import org.apache.jena.sparql.core.Var;

public class AggResultSetPart
    implements Agg<ResultSetPart>
{
    private List<String> varNames;

    public AggResultSetPart(List<String> varNames) {
        this.varNames = varNames;
    }

    @Override
    public Acc<ResultSetPart> createAccumulator() {
        Acc<ResultSetPart> result = new AccResultSetPart(varNames);
        return result;
    }

    /**
     *
     */
    @Override
    public Set<Var> getDeclaredVars() {
        //return null;
        Set<Var> result = VarUtils.toSet(varNames);
        return result;
    }

}
