package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Template;


/**
 * An extension of a (triple-based) construct template where:
 * - a set of nodes can be marked as 'entity nodes' which are starting points for traversing the template.
 * - every blank node can be mapped to a tuple of expressions whose evaluation over a binding
 *   yields an identity of the blank node.
 * 
 * 
 * 
 * @author raven
 *
 */
public interface EntityTemplate {

    EntityTemplate cloneTemplate();

    Template getTemplate();
    void setTemplate(Template template);

    List<Node> getEntityNodes();
//    void setEntityNode(Node rootVar);

    Map<Node, ExprList> getBnodeIdMapping();

//    default void setEntityNode(String rootVarName) {
//        setEntityNode(Var.alloc(rootVarName));
//    }

    EntityTemplate applyNodeTransform(NodeTransform nodeTransform);


    /**
     * Combine the information from multiple EntityTemplates into a single one
     * This operation should only be performed after processing variable names
     * such that no clashes occur.
     *
     * @param templates
     * @return
     */
    static EntityTemplate merge(EntityTemplate ... templates) {
        Set<Triple> triples = new LinkedHashSet<>();
        Set<Node> entityNodes = new LinkedHashSet<>();
        Map<Node, ExprList> bnodeIdMapping = new LinkedHashMap<>();

        for (EntityTemplate template : templates) {
            triples.addAll(template.getTemplate().getBGP().getList());
            entityNodes.addAll(template.getEntityNodes());

            // TODO Ensure there is are no clashes
            bnodeIdMapping.putAll(template.getBnodeIdMapping());
        }

        EntityTemplateImpl result = new EntityTemplateImpl(
                new ArrayList<>(entityNodes),
                new Template(BasicPattern.wrap(new ArrayList<>(triples))),
                bnodeIdMapping);

        return result;
    }
}
