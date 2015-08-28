package org.aksw.jena_sparql_api.batch;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.StepRelation;
import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.util.ExprUtils;


public class ResourceShapeBuilder {
    private ResourceShape resourceShape;
    private PrefixMapping prefixMapping;

    public ResourceShapeBuilder(PrefixMapping prefixMapping) {
        this(new ResourceShape(), prefixMapping);
    }

    public ResourceShapeBuilder(ResourceShape resourceShape) {
        this(resourceShape, new PrefixMappingImpl());
    }

    public ResourceShapeBuilder(ResourceShape resourceShape, PrefixMapping prefixMapping) {
        this.resourceShape = resourceShape;
        this.prefixMapping = prefixMapping;
    }
    
    public ResourceShape getResourceShape() {
        return resourceShape;
    }

    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    public ResourceShapeBuilder outgoing(String propertyUri) {
        ResourceShapeBuilder result = nav(propertyUri, false);
        return result;
    }
    
    public ResourceShapeBuilder outgoing(Node property) {
        ResourceShapeBuilder result = nav(property, false);
        return result;
    }
    
    
    public ResourceShapeBuilder outgoing(Expr expr) {
        ResourceShapeBuilder result = nav(expr, false);
        return result;
    }
    
    
    public ResourceShapeBuilder outgoing(Relation relation) {
        ResourceShapeBuilder result = nav(relation, false);
        return result;
    }
    
    public ResourceShapeBuilder incoming(String propertyUri) {
        ResourceShapeBuilder result = nav(propertyUri, true);
        return result;
    }
    
    public ResourceShapeBuilder incoming(Node property) {
        ResourceShapeBuilder result = nav(property, true);
        return result;
    }
    
    
    public ResourceShapeBuilder incoming(Expr expr) {
        ResourceShapeBuilder result = nav(expr, true);
        return result;        
    }
    
    
    public ResourceShapeBuilder incoming(Relation relation) {
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


    public ResourceShapeBuilder nav(Expr expr, boolean isInverse) {
        Relation relation = new Relation(new ElementFilter(expr), Vars.p, Vars.o);
        ResourceShapeBuilder result = nav(relation, isInverse);
        return result;        
    }


    public ResourceShapeBuilder nav(StepRelation step) {
        ResourceShapeBuilder result = nav(step.getRelation(), step.isInverse());
        return result;
    }

    public ResourceShapeBuilder nav(Relation relation, boolean isInverse) {
        Map<Relation, ResourceShape> map = isInverse
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
