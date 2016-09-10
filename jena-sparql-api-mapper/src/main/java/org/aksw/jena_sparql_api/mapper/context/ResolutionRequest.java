package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.apache.jena.graph.Node;

public class ResolutionRequest {
    //protected Map<String, Object> entity;
    protected EntityOps entityOps;
    protected Object entity;
    protected String propertyName;
    protected Node node;
    
    // If no type is provided, the default mapping for the property's type will be used
    protected RdfType type;
    
    public ResolutionRequest(EntityOps entityOps, Object entity, String propertyName, Node node, RdfType type) {
        super();
        this.entityOps = entityOps;
        this.entity = entity;
        this.propertyName = propertyName;
        this.node = node;
        this.type = type;
    }

    public EntityOps getEntityOps() {
        return entityOps;
    }
    
    public Object getEntity() {
        return entity;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Node getNode() {
        return node;
    }

    public RdfType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ResolutionRequest [entity=" + entity + ", propertyName="
                + propertyName + ", node=" + node + ", type=" + type + "]";
    }
    
    
    
    
    
    
//    protected Object entity;
//    protected RdfType rdfType;
//    protected Supplier<Node> iriGenerator;
//    
//    public ResolutionRequest(Object entity, RdfType rdfType,
//            Supplier<Node> iriGenerator) {
//        super();
//        this.entity = entity;
//        this.rdfType = rdfType;
//        this.iriGenerator = iriGenerator;
//    }
//
//    public Object getEntity() {
//        return entity;
//    }
//
//    public RdfType getRdfType() {
//        return rdfType;
//    }
//
//    public Supplier<Node> getIriGenerator() {
//        return iriGenerator;
//    }
//
//    @Override
//    public String toString() {
//        return "ResolutionRequest [entity=" + entity + ", rdfType=" + rdfType
//                + ", iriGenerator=" + iriGenerator + "]";
//    }
}
