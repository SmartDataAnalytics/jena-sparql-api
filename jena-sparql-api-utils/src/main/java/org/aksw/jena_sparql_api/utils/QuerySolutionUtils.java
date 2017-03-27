package org.aksw.jena_sparql_api.utils;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

public class QuerySolutionUtils {
    public static QuerySolution rename(QuerySolution qs, Map<Var, Var> varMap) {
        QuerySolutionMap result = new QuerySolutionMap();

        Iterator<String> itVarNames = qs.varNames();

        while(itVarNames.hasNext()) {
            String varName = itVarNames.next();

            RDFNode rdfNode = qs.get(varName);

            Var sourceVar = Var.alloc(varName);
            Var targetVar = varMap.get(sourceVar);
            if(targetVar == null) {
                targetVar = sourceVar;
            }

            String targetVarName = targetVar.getVarName();
            result.add(targetVarName, rdfNode);
        }

        return result;
    }
}
