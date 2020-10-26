package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.rx.EntityBaseQuery;


/** Basic implementation of {@link EntityQueryBasic} */
public class EntityQueryImpl
{
    protected EntityBaseQuery baseQuery;
    protected List<GraphPartitionJoin> auxiliaryGraphPartitions;
    protected List<GraphPartitionJoin> optionalJoins;


    public EntityQueryImpl() {
        this(null, new ArrayList<>(), new ArrayList<>());
    }

    public EntityQueryImpl(EntityBaseQuery baseQuery, List<GraphPartitionJoin> auxiliaryGraphPartitions, List<GraphPartitionJoin> optionalJoins) {
        super();
        this.baseQuery = baseQuery;
        this.auxiliaryGraphPartitions = auxiliaryGraphPartitions;
        this.optionalJoins = optionalJoins;
    }

    public EntityBaseQuery getBaseQuery() {
        return baseQuery;
    }

    public void setBaseQuery(EntityBaseQuery baseQuery) {
        this.baseQuery = baseQuery;
    }

    public List<GraphPartitionJoin> getAuxiliaryGraphPartitions() {
        return auxiliaryGraphPartitions;
    }

    public List<GraphPartitionJoin> getOptionalJoins() {
        return optionalJoins;
    }

    public void setOptionalJoins(List<GraphPartitionJoin> optionalJoins) {
        this.optionalJoins = optionalJoins;
    }
}


