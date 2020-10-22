package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;


/** Basic implementation of {@link PartitionedQuery} */
public class PartitionedQueryImpl
    implements PartitionedQuery
{
    protected Query standardQuery;
    protected Map<Node, ExprList> idMapping = new HashMap<>();
    protected List<Var> partitionVars = new ArrayList<>();
    protected Node rootVar = null;

    public PartitionedQueryImpl(Query standardQuery) {
        this.standardQuery = standardQuery;
    }

    public Query toStandardQuery() {
        return standardQuery;
    }

    public Map<Node, ExprList> getIdMapping() {
        return idMapping;
    }

    public List<Var> getPartitionVars() {
        return partitionVars;
    }

    public Node getRootNode() {
        return rootVar;
    }

    public void setRootNode(Node rootVar) {
        this.rootVar = rootVar;
    }
}


