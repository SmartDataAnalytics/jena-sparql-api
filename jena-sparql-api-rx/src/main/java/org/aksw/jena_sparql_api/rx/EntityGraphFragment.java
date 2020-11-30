package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplate;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

/**
 * A simplified construct query that is just comprised of a sequence of
 * entity variables, a graph pattern and a template.
 * I.e. a mapping of a relation to triples with designated partition variables
 *
 * @author raven
 *
 */
public class EntityGraphFragment {
    protected List<Var> partitionVars;
    protected EntityTemplate entityTemplate;
    protected Element element;

    public EntityGraphFragment() {
        this(new ArrayList<>(), new EntityTemplateImpl(), null);
    }

    public EntityGraphFragment(List<Var> partitionVars, EntityTemplate entityTemplate, Element element) {
        super();
        this.partitionVars = partitionVars;
        this.entityTemplate = entityTemplate;
        this.element = element;
    }

    public static EntityGraphFragment empty(List<Var> partitionVars) {
        return new EntityGraphFragment(partitionVars,
                new EntityTemplateImpl(),
                new ElementGroup());
    }

    public static EntityGraphFragment fromQuery(Var entityVar, Query query) {
        return fromQuery(Arrays.asList(entityVar), Arrays.asList(entityVar), query);
    }

    public static EntityGraphFragment fromQuery(List<Var> partitionVars, List<Node> entityNodes, Query query) {
        EntityGraphFragment result = new EntityGraphFragment(
              partitionVars,
              new EntityTemplateImpl(entityNodes, query.getConstructTemplate()),
              query.getQueryPattern());

        return result;
    }

    public EntityTemplate getEntityTemplate() {
        return entityTemplate;
    }

    public void setEntityTemplate(EntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public List<Var> getPartitionVars() {
        return partitionVars;
    }

    public void setPartitionVars(List<Var> entityVars) {
        this.partitionVars = entityVars;
    }

//    @Override
    public EntityGraphFragment applyNodeTransform(NodeTransform nodeTransform) {
        EntityGraphFragment result = new EntityGraphFragment(
                NodeTransformLib.transformVars(nodeTransform, partitionVars),
                entityTemplate.applyNodeTransform(nodeTransform),
                ElementUtils.applyNodeTransform(element, nodeTransform));

        return result;
    }


    @Override
    public String toString() {
        String result
                = "ENTITY " + partitionVars + "\n"
                + "CONSTRUCT " + entityTemplate + "\n"
                + "WHERE " + element;

        return result;
    }

}
