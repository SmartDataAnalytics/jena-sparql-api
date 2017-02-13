package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.impl.type.PlaceholderInfo;
import org.aksw.jena_sparql_api.mapper.impl.type.ResourceFragment;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.h2.util.StringUtils;

public abstract class RdfMapperPropertyBase
    implements RdfMapperProperty
{
    //protected RdfClass rdfClass;
    //protected  collectiontype

    //protected PropertyDescriptor propertyDescription;

    /**
     * The (java) name of the attribute
     */
    //protected EntityOps entityOps;
    //protected String propertyName;
    protected PropertyOps propertyOps;
    protected Property predicate;
    //protected BeanWrapper owningBean;

    /**
     * The corresponding RDF predicate
     */
//    protected Relation relation;

    protected RdfType targetRdfType;

    transient protected Set<String> propertyNames;
//    protected String fetchMode;

    // Optional function for creating an Iri (Node) for a given target value
    protected BiFunction<Object, Object, Node> createTargetNode;

//    /**
//     * The type can be either simply a class (including primitive ones), but it can also be
//     * a parameterized class, such as a List&lt;Person&gt;
//     *
//     */
//    protected RdfType rdfType;


    public RdfMapperPropertyBase(PropertyOps propertyOps, Property predicate, RdfType targetRdfType, BiFunction<Object, Object, Node> createTargetNode) { //, String fetchMode) {
        super();
        //this.propertyName = propertyName;
        this.propertyOps = propertyOps;
        this.predicate = predicate;
        this.targetRdfType = targetRdfType;

        this.propertyNames = Collections.singleton(propertyOps.getName());
        this.createTargetNode = createTargetNode;
    }

    @Override
    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {

        Resource s = out.getResource();
        Model tmp = s.getModel();
        Property p = tmp.createProperty(predicate.getURI());
        Resource o = tmp.createResource();

        Object v = propertyOps.getValue(entity);

        
        PlaceholderInfo info = new PlaceholderInfo(null, targetRdfType, entity, null, propertyOps, v, null, this);
        
//        Map<RDFNode, Object> placeholders = new HashMap<>();
//        placeholders.put(o, v);
        
        out.getPlaceholders().put(o, info);
    }
//
//    @Override
//    public void readFragment(Object tgtEntity, ResourceFragment inout) {
//        Resource r = inout.getResource();
//        r.getProperty(property);
//
//        
//        inout.getPlaceholders()
//        Node node;
//        if(t != null) {
//            node = t.getObject();
//            outSink.accept(t);
//        } else {
//            node = null;
//        }
//
//        if(node == null) {
//        	if(createTargetNode != null) {
//	        	Object childEntity = propertyOps.getValue(entity);
//	        	if(childEntity != null) {
//	        		node = createTargetNode.apply(entity, childEntity);
//	        	}
//        	}
//        }
//        
//        if(node != null) {
//        	persistenceContext.requestResolution(propertyOps, entity, node);
//        }
//     }
    
//    public String getPropertyName() {
//        return propertyOps.getName();
//    }

//    public Relation getRelation() {
//        return relation;
//    }

//    public RdfType getTargetRdfType() {
//        return targetRdfType;
//    }

    @Override
    public PropertyOps getPropertyOps() {
        return propertyOps;
    }

    @Override
    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    public Node getTargetNode(String subjectUri, Object entity) {
        Node result = targetRdfType.hasIdentity()
                ? targetRdfType.getRootNode(entity)
                : NodeFactory.createURI(subjectUri + "/" + StringUtils.urlEncode(propertyOps.getName()));
        return result;
    }

}
