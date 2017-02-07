package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.impl.type.UnresolvedResource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
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


    public RdfMapperPropertyBase(PropertyOps propertyOps, Node predicate, RdfType targetRdfType, BiFunction<Object, Object, Node> createTargetNode) { //, String fetchMode) {
        super();
        //this.propertyName = propertyName;
        this.propertyOps = propertyOps;
        this.predicate = predicate;
        this.targetRdfType = targetRdfType;

        this.propertyNames = Collections.singleton(propertyOps.getName());
        this.createTargetNode = createTargetNode;
    }

    @Override
    public void exposeFragment(UnresolvedResource out, Object entity) {

        Resource s = out.getResource();
        Model tmp = s.getModel();
        Property p = tmp.createProperty(predicate.getURI());
        Resource o = tmp.createResource();

        Object v = propertyOps.getValue(entity);

        Map<RDFNode, Object> placeholders = new HashMap<>();
        placeholders.put(o, v);

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

    public Node getTargetNode(String subjectUri, Object entity) {
        Node result = targetRdfType.hasIdentity()
                ? targetRdfType.getRootNode(entity)
                : NodeFactory.createURI(subjectUri + "/" + StringUtils.urlEncode(propertyOps.getName()));
        return result;
    }

}
