package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.springframework.beans.BeanWrapper;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public class RdfProperyObject
    implements RdfProperty
{
    //protected RdfClass rdfClass;
    //protected  collectiontype

    //protected PropertyDescriptor propertyDescription;

    /**
     * The (java) name of the attribute
     */
    protected String propertyName;

    protected BeanWrapper owningBean;

    /**
     * The corresponding RDF predicate
     */
    protected Relation relation;

    protected RdfClass targetRdfClass;

    /**
     * The type can be either simply a class (including primitive ones), but it can also be
     * a parameterized class, such as a List&lt;Person&gt;
     *
     */
    protected RdfType rdfType;


    public RdfProperyObject(String name, Relation relation, RdfClass targetRdfClass) {
        super();
        this.propertyName = name;
        this.relation = relation;
        this.targetRdfClass = targetRdfClass;
    }

    public String getName() {
        return propertyName;
    }

    public Relation getRelation() {
        return relation;
    }

    public RdfClass getTargetRdfClass() {
        return targetRdfClass;
    }


    @Override
    public void writePropertyValue(Object obj, Node subject, Graph outputGraph) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object readPropertyValue(Graph graph, Node subject) {
        // TODO Auto-generated method stub
        return null;
    }
}
