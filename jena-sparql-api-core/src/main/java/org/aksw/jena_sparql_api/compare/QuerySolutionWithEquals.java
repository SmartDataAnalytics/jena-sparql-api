package org.aksw.jena_sparql_api.compare;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.QuerySolutionBase;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/20/12
 *         Time: 2:30 PM
 */
public class QuerySolutionWithEquals
    extends QuerySolutionBase
{
    private QuerySolution querySolution;
    private Map<String, RDFNode> map;

    public QuerySolutionWithEquals(QuerySolution querySolution) {
        this.querySolution = querySolution;
        this.map = createMap(querySolution);
    }

    @Override
    protected RDFNode _get(String varName) {
        return querySolution.get(varName);
    }

    @Override
    protected boolean _contains(String varName) {
        return querySolution.contains(varName);
    }

    @Override
    public Iterator<String> varNames() {
        return querySolution.varNames();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuerySolutionWithEquals that = (QuerySolutionWithEquals) o;

        boolean result = this.map.equals(that.map);
        return  result;
    }

    public static Map<String, RDFNode> createMap(QuerySolution querySolution) {
        Map<String, RDFNode> result = new HashMap<String, RDFNode>();
        Iterator<String> it = querySolution.varNames();
        while(it.hasNext()) {
            String varName = it.next();
            result.put(varName, querySolution.get(varName));
        }

        return result;
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {
        return "" + map;
    }

}
