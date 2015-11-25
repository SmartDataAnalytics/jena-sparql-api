package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collections;
import java.util.Set;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Node;

public abstract class RdfPopulatorPropertyBase
    implements RdfPopulatorProperty
{
    //protected RdfClass rdfClass;
    //protected  collectiontype

    //protected PropertyDescriptor propertyDescription;

    /**
     * The (java) name of the attribute
     */
    protected String propertyName;
    protected Node predicate;
    //protected BeanWrapper owningBean;

    /**
     * The corresponding RDF predicate
     */
//    protected Relation relation;

    protected RdfType targetRdfType;

//    protected String fetchMode;

//    /**
//     * The type can be either simply a class (including primitive ones), but it can also be
//     * a parameterized class, such as a List&lt;Person&gt;
//     *
//     */
//    protected RdfType rdfType;


    public RdfPopulatorPropertyBase(String propertyName, Node predicate, RdfType targetRdfType) { //, String fetchMode) {
        super();
        this.propertyName = propertyName;
        this.predicate = predicate;
        this.targetRdfType = targetRdfType;
//        this.fetchMode = fetchMode;
    }

    public String getPropertyName() {
        return propertyName;
    }

//    public Relation getRelation() {
//        return relation;
//    }

//    public RdfType getTargetRdfType() {
//        return targetRdfType;
//    }


	@Override
	public Set<String> getPropertyNames() {
		Set<String> result = Collections.singleton(propertyName);
		return result;
	}


}
