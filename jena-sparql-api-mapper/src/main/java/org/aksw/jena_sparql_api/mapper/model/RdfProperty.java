package org.aksw.jena_sparql_api.mapper.model;

import org.aksw.jena_sparql_api.concepts.Relation;

public class RdfProperty {
    //protected RdfClass rdfClass;
    //protected  collectiontype

    /**
     * The name of the attribute
     */
    protected String name;

    /**
     * The corresponding RDF predicate
     */
    protected Relation relation;

    protected RdfClassImpl targetRdfClass;


    public RdfProperty(String name, Relation relation, RdfClassImpl targetRdfClass) {
        super();
        this.name = name;
        this.relation = relation;
        this.targetRdfClass = targetRdfClass;
    }

    public String getName() {
        return name;
    }

    public Relation getRelation() {
        return relation;
    }

    public RdfClassImpl getTargetRdfClass() {
        return targetRdfClass;
    }
}
