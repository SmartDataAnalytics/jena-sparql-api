package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;

/**
 * A query with a sequence of designated partition variables
 *
 * @author raven
 *
 */
public class EntityBaseQuery {
    /**
     * The standard query can be SPARQL SELECT query
     *
     */
    protected Query standardQuery;

    protected EntityTemplate entityTemplate;
    protected List<Var> partitionVars;
    protected List<SortCondition> partitionOrderBy;

    public EntityBaseQuery(
            List<Var> partitionVars,
            EntityTemplate entityTemplate,
            Query standardQuery) {
        this(partitionVars, entityTemplate, standardQuery, new ArrayList<>());
    }

    public EntityBaseQuery(
            List<Var> partitionVars,
            EntityTemplate entityTemplate,
            Query standardQuery,
            List<SortCondition> partitionOrderBy) {
        super();
        this.standardQuery = standardQuery;
        this.entityTemplate = entityTemplate;
        this.partitionVars = partitionVars;
        this.partitionOrderBy = partitionOrderBy;
    }

    public Query getStandardQuery() {
        return standardQuery;
    }

    public EntityTemplate getEntityTemplate() {
        return entityTemplate;
    }

    public void setEntityTemplate(EntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    public void setStandardQuery(Query standardQuery) {
        this.standardQuery = standardQuery;
    }

    public List<Var> getPartitionVars() {
        return partitionVars;
    }

    public void setPartitionVars(List<Var> partitionVars) {
        this.partitionVars = partitionVars;
    }

    public List<SortCondition> getPartitionOrderBy() {
        return partitionOrderBy;
    }

    @Override
    public String toString() {
        String result
            = "ENTITY " + partitionVars + "\n"
            + "CONSTRUCT " + entityTemplate + "\n"
            + "WHERE " + standardQuery + "\n"
            + "ORDER ENTITIES BY " + partitionOrderBy;

        return result;
    }
}
