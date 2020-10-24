package org.aksw.jena_sparql_api.rx;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Template;

public interface GraphPartitionBase {
    Template getTemplate();
    void setTemplate(Template template);

    Node getEntityNode();
    void setEntityNode(Node rootVar);

    Map<Node, ExprList> getBnodeIdMapping();

    default void setEntityNode(String rootVarName) {
        setEntityNode(Var.alloc(rootVarName));
    }
}
