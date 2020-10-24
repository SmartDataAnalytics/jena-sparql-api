package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;


/** Basic implementation of {@link EntityQuery} */
public class EntityQueryImpl
    implements EntityQuery
{
    protected Query selectQuery;
//    protected Map<Node, ExprList> idMapping = new HashMap<>();
//    protected Node entityNode = null;

    protected GraphPartitionBase directGraphPartition = new DirectGraphPartitionImpl();
    protected List<Var> partitionVars = new ArrayList<>();
    protected List<SortCondition> partitionOrderBy = new ArrayList<SortCondition>();
//
//    public EntityQueryImpl(Query standardQuery) {
//        this.standardQuery = standardQuery;
//    }
//
//    public Query getConstructMemberQuery() {
//        return standardQuery;
//    }

    public List<Var> getPartitionVars() {
        return partitionVars;
    }

//    public Node getEntityNode() {
//        return entityNode;
//    }
//
//    public void setEntityNode(Node entityNode) {
//        this.entityNode = entityNode;
//    }

    @Override
    public List<SortCondition> getPartitionOrderBy() {
        return partitionOrderBy;
    }

    @Override
    public GraphPartitionBase getDirectGraphPartition() {
        return directGraphPartition;
    }

    @Override
    public Query getPartitionSelectorQuery() {
        return selectQuery;
    }

    @Override
    public void setPartitionSelectorQuery(Query selectQuery) {
        this.selectQuery = selectQuery;
    }
}


