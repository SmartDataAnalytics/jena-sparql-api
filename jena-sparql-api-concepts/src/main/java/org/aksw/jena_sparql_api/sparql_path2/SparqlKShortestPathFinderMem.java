package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

public class SparqlKShortestPathFinderMem
    implements SparqlKShortestPathFinder
{
    protected QueryExecutionFactory qef;

    public SparqlKShortestPathFinderMem(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    @Override
    public Iterator<TripletPath<Node, Directed<Node>>> findPaths(Node start, Node end, Path path, Long k) {

        final List<NestedPath<Node, Node>> rdfPaths = new ArrayList<>();

        PathExecutionUtils.executePath(path, start, end, qef, p -> {
            rdfPaths.add(p);
            boolean r = k == null ? false : rdfPaths.size() >= k;
            return r; });

        Iterator<NestedPath<Node, Node>> result = rdfPaths.iterator();
        //return result;
        return null;
    }
}
