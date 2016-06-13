package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;

public class ConceptBuilderAnd
    extends ConceptBuilderListBase
{
    public ConceptBuilderAnd() {
        this(null);
    }

//    public ConceptBuilderAnd() {
//        //this(new ArrayList<ConceptBuilder>());
//    }

    public ConceptBuilderAnd(RestrictionBuilder parent) {
        super(parent);
    }


    public ConceptBuilderUnion addMember(ConceptBuilder member) {
        this.members.add(member);
    }

}
