package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.List;

import org.aksw.jena_sparql_api.rx.EntityBaseQuery;


/** Basic implementation of {@link EntityQueryBasic} */
public class EntityQueryImpl
{
    protected EntityBaseQuery baseQuery;
    protected AttributeGraphFragment attributePart;

    public EntityQueryImpl() {
        this(null, new AttributeGraphFragment());
    }

    public EntityQueryImpl(EntityBaseQuery baseQuery, AttributeGraphFragment attributePart) {
        super();
        this.baseQuery = baseQuery;
        this.attributePart = attributePart;
    }

    public EntityBaseQuery getBaseQuery() {
        return baseQuery;
    }

    public void setBaseQuery(EntityBaseQuery baseQuery) {
        this.baseQuery = baseQuery;
    }

    public AttributeGraphFragment getAttributePart() {
        return attributePart;
    }

    public void setAttributePart(AttributeGraphFragment attributePart) {
        this.attributePart = attributePart;
    }

    public List<GraphPartitionJoin> getMandatoryJoins() {
        return attributePart.getMandatoryJoins();
    }

    public List<GraphPartitionJoin> getOptionalJoins() {
        return attributePart.getOptionalJoins();
    }

    public void setOptionalJoins(List<GraphPartitionJoin> optionalJoins) {
        this.attributePart.setOptionalJoins(optionalJoins);
    }
}


