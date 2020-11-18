package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
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
public class EntityTemplateImpl
    implements EntityTemplate
{
    protected List<Node> entityNodes;
    protected Template template;
    protected Map<Node, ExprList> bnodeIdMapping;

    public static Map<Node, ExprList> applyNodeTransformBnodeMap(NodeTransform nodeTransform, Map<Node, ExprList> map) {
        Map<Node, ExprList> result = map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> nodeTransform.apply(e.getKey()),
                        e -> NodeTransformLib.transform(nodeTransform, e.getValue())));
        return result;
    }

    public EntityTemplateImpl() {
        this(new ArrayList<>(), new Template(new BasicPattern()));
    }


    public EntityTemplateImpl(List<Node> entityNodes, Template template) {
        this(entityNodes, template, new LinkedHashMap<>());
    }

    public EntityTemplateImpl(List<Node> entityNodes, Template template, Map<Node, ExprList> bnodeIdMapping) {
        super();
        this.entityNodes = entityNodes;
        this.template = template;
        this.bnodeIdMapping = bnodeIdMapping;
    }

    public EntityTemplateImpl cloneTemplate() {
        return new EntityTemplateImpl(
                new ArrayList<>(entityNodes),
                new Template(BasicPattern.wrap(new ArrayList<>(template.getTriples()))),
                new LinkedHashMap<>(bnodeIdMapping));
    }

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
    public List<Node> getEntityNodes() {
        return entityNodes;
    }

//    @Override
//    public void setEntityNode(Node entityNode) {
//        this.entityNode = entityNode;
//    }

    @Override
    public Map<Node, ExprList> getBnodeIdMapping() {
        return bnodeIdMapping;
    }

    @Override
    public void setTemplate(Template template) {
        this.template = template;
    }

    public static List<Node> transformNodes(NodeTransform nodeTransform, List<? extends Node> varList) {
        List<Node> varList2 = new ArrayList<>(varList.size()) ;
        for ( Node v : varList ) {
            Node v2 = nodeTransform.apply(v) ;
            varList2.add(v2) ;
        }
        return varList2 ;
    }

    @Override
    public EntityTemplate applyNodeTransform(NodeTransform nodeTransform) {
        return new EntityTemplateImpl(
                transformNodes(nodeTransform, entityNodes),
                new Template(NodeTransformLib.transform(nodeTransform, template.getBGP())),
                applyNodeTransformBnodeMap(nodeTransform, bnodeIdMapping)
        );
    }


    @Override
    public String toString() {
        return template.getGraphPattern() + ", entity nodes " + entityNodes + ", bnodeIdMapping " + bnodeIdMapping;
    }

}
