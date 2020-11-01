package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplate;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Template;

/**
 * A query with a sequence of designated partition variables
 *
 * @author raven
 *
 */
public class EntityBaseQuery
    implements Cloneable
{
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

//    @Override
    public EntityBaseQuery cloneQuery() {
        return new EntityBaseQuery(
                new ArrayList<>(partitionVars),
                entityTemplate.cloneTemplate(),
                standardQuery.cloneQuery(),
                new ArrayList<>(partitionOrderBy));
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

    public static EntityBaseQuery create(Var partitionAndEntityVar, Query standardQuery) {
        Query partitionSelect = standardQuery.cloneQuery();
        partitionSelect.setQuerySelectType();
        partitionSelect.setQueryResultStar(true);

        List<Var> partitionVars = Collections.singletonList(partitionAndEntityVar);
        List<Node> entityNodes = Collections.<Node>singletonList(partitionAndEntityVar);

        Template template = standardQuery.getConstructTemplate();
        if (template == null) {
            template = new Template(new BasicPattern());
        }

        EntityTemplateImpl et = new EntityTemplateImpl(
                entityNodes,
                template);

        EntityBaseQuery result = new EntityBaseQuery(partitionVars, et, partitionSelect);

        return result;
    }
}

