package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.apache.jena.graph.Node;

public abstract class RdfPopulatorPropertyBase
    implements RdfPopulatorProperty
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
    protected Node predicate;
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


    public RdfPopulatorPropertyBase(PropertyOps propertyOps, Node predicate, RdfType targetRdfType, BiFunction<Object, Object, Node> createTargetNode) { //, String fetchMode) {
        super();
        //this.propertyName = propertyName;
        this.propertyOps = propertyOps;
        this.predicate = predicate;
        this.targetRdfType = targetRdfType;
        
        this.propertyNames = Collections.singleton(propertyOps.getName());
        this.createTargetNode = createTargetNode;
    }


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


}
