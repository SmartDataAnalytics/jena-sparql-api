package org.aksw.jena_sparql_api.utils;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;

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
