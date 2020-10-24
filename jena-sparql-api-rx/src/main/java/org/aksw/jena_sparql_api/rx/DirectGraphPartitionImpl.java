package org.aksw.jena_sparql_api.rx;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Template;

/**
 * In an entity query the construct/entity part this is based directly on
 * the entity selector SELECT query
 *
 * As such, it neither declares its own WHERE pattern nor partition variables
 * as they are based directly on the selector query.
 *
 *
 * @author raven
 *
 */
public class DirectGraphPartitionImpl
    implements GraphPartitionBase
{

    protected Node entityNode;
    protected Template template;
    protected Map<Node, ExprList> bnodeIdMapping = new LinkedHashMap<>();

    /**
     * Template to construct graphs directly from the given select
     * query (avoids having to repeat the select query's pattern as a graph partition)
     *
     * @return
     */
    public Template getTemplate() {
        return template;
    }

    @Override
    public Node getEntityNode() {
        return entityNode;
    }

    @Override
    public void setEntityNode(Node entityNode) {
        this.entityNode = entityNode;
    }

    @Override
    public Map<Node, ExprList> getBnodeIdMapping() {
        return bnodeIdMapping;
    }

    @Override
    public void setTemplate(Template template) {
        this.template = template;
    }

}
