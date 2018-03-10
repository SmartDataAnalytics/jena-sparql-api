package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;

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
    protected TypeConverter typeConverter;



//    /**
//     * The type can be either simply a class (including primitive ones), but it can also be
//     * a parameterized class, such as a List&lt;Person&gt;
//     *
//     */
//    protected RdfType rdfType;


    public RdfMapperPropertyBase(PropertyOps propertyOps, Property predicate, RdfType targetRdfType, BiFunction<Object, Object, Node> createTargetNode, TypeConverter typeConverter) { //, String fetchMode) {
        super();
        Objects.requireNonNull(predicate);
        
        //this.propertyName = propertyName;
        this.propertyOps = propertyOps;
        this.predicate = predicate;
        this.targetRdfType = targetRdfType;

        this.propertyNames = Collections.singleton(propertyOps.getName());
        this.createTargetNode = createTargetNode;

        this.typeConverter = typeConverter;
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
                : NodeFactory.createURI(subjectUri + "-" + StringUtils.urlEncode(propertyOps.getName()));
        return result;
    }

}
