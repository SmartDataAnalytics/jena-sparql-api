package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Template;

public class ObjectQueryImpl
    extends ObjectQueryBase
{
    protected Template template;
    protected Relation relation;

    public ObjectQueryImpl(Template template, Relation relation) {
        this(template, relation, new HashMap<>());
    }

    public ObjectQueryImpl(Template template, Relation relation, Map<Node, ExprList> idMapping) {
        super(idMapping);
        this.template = template;
        this.relation = relation;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "ObjectQueryImpl [template=" + template + ", relation=" + relation + ", idMapping=" + idMapping + "]";
    }
}

