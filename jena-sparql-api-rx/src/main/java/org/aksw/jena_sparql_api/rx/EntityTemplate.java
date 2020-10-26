package org.aksw.jena_sparql_api.rx;

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

public interface EntityTemplate {
    Template getTemplate();
    void setTemplate(Template template);

    List<Node> getEntityNodes();
//    void setEntityNode(Node rootVar);

    Map<Node, ExprList> getBnodeIdMapping();

//    default void setEntityNode(String rootVarName) {
//        setEntityNode(Var.alloc(rootVarName));
//    }

    EntityTemplate applyNodeTransform(NodeTransform nodeTransform);


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
