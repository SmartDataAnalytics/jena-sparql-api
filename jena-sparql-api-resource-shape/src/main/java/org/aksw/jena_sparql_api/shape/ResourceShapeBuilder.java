package org.aksw.jena_sparql_api.shape;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ExprUtils;


public class ResourceShapeBuilder {
	protected ResourceShapeBuilder parent;

    protected ResourceShape resourceShape;
    protected PrefixMapping prefixMapping;

    public ResourceShapeBuilder() {
        this(new Prologue());
    }

    public ResourceShapeBuilder(Prologue prologue) {
        this(prologue.getPrefixMapping());
    }

    public ResourceShapeBuilder(PrefixMapping prefixMapping) {
        this(new ResourceShape(), prefixMapping);
    }

    public ResourceShapeBuilder(ResourceShape resourceShape) {
        this(resourceShape, new PrefixMappingImpl());
    }

    public ResourceShapeBuilder(ResourceShape resourceShape, PrefixMapping prefixMapping) {
        this(null, resourceShape, prefixMapping);
    }

    public ResourceShapeBuilder(ResourceShapeBuilder parent, ResourceShape resourceShape, PrefixMapping prefixMapping) {
        this.parent = parent;
    	this.resourceShape = resourceShape;
        this.prefixMapping = prefixMapping;
    }

    public ResourceShape getResourceShape() {
        return resourceShape;
    }

    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    public ResourceShapeBuilder filter(Node node) {
    	Expr expr = NodeValue.makeNode(node);
    	ResourceShapeBuilder result = filter(expr);
    	return result;
    }

    public ResourceShapeBuilder filter(Expr expr) {
    	Concept concept = null;
    	ResourceShapeBuilder result = filter(concept);
    	return result;

//    	Concept.cr
//
//    	ResourceShapeBuilder result = filter(expr);
//    	return result;
    }

    public ResourceShapeBuilder filter(Concept concept) {
    	return null;
    }


    public ResourceShapeBuilder out(String propertyUri) {
        ResourceShapeBuilder result = nav(propertyUri, false);
        return result;
    }

    public ResourceShapeBuilder out(Node property) {
        ResourceShapeBuilder result = nav(property, false);
        return result;
    }

    public ResourceShapeBuilder out(Property property) {
        ResourceShapeBuilder result = nav(property, false);
        return result;
    }


    public ResourceShapeBuilder out(Expr expr) {
        ResourceShapeBuilder result = nav(expr, false);
        return result;
    }


    public ResourceShapeBuilder out(BinaryRelation relation) {
        ResourceShapeBuilder result = nav(relation, false);
        return result;
    }

    public ResourceShapeBuilder in(String propertyUri) {
        ResourceShapeBuilder result = nav(propertyUri, true);
        return result;
    }

    public ResourceShapeBuilder in(Node property) {
        ResourceShapeBuilder result = nav(property, true);
        return result;
    }

    public ResourceShapeBuilder in(Property property) {
        ResourceShapeBuilder result = nav(property, false);
        return result;
    }

    public ResourceShapeBuilder in(Expr expr) {
        ResourceShapeBuilder result = nav(expr, true);
        return result;
    }


    public ResourceShapeBuilder in(BinaryRelation relation) {
        ResourceShapeBuilder result = nav(relation, true);
        return result;
    }

    public ResourceShapeBuilder nav(String propertyUri, boolean isInverse) {
        String p = prefixMapping.expandPrefix(propertyUri);

        Node node = NodeFactory.createURI(p);
        ResourceShapeBuilder result = nav(node, isInverse);
        return result;
    }


    public ResourceShapeBuilder nav(Node property, boolean isInverse) {
        Expr expr = new E_Equals(new ExprVar(Vars.p), ExprUtils.nodeToExpr(property));
        ResourceShapeBuilder result = nav(expr, isInverse);
        return result;
    }

    public ResourceShapeBuilder nav(Property property, boolean isInverse) {
        ResourceShapeBuilder result = nav(property.asNode(), isInverse);
        return result;
    }


    public ResourceShapeBuilder nav(Expr expr, boolean isInverse) {
        BinaryRelation relation = new BinaryRelationImpl(new ElementFilter(expr), Vars.p, Vars.o);
        ResourceShapeBuilder result = nav(relation, isInverse);
        return result;
    }


    public ResourceShapeBuilder nav(StepRelation step) {
        ResourceShapeBuilder result = nav(step.getRelation(), step.isInverse());
        return result;
    }

    public ResourceShapeBuilder nav(BinaryRelation relation, boolean isInverse) {
        Map<BinaryRelation, ResourceShape> map = isInverse
                ? resourceShape.getIngoing()
                : resourceShape.getOutgoing();


        ResourceShape rs = map.get(relation);
        if(rs == null) {
            rs = new ResourceShape();
            map.put(relation, rs);
        }

        ResourceShapeBuilder result = new ResourceShapeBuilder(rs, prefixMapping);
        return result;

    }
}
