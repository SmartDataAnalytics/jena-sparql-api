package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;

public class ConceptBuilderUnion
    extends ConceptBuilderListBase
{
    public ConceptBuilderUnion() {
        this(null);
    }

    public ConceptBuilderUnion(RestrictionBuilder parent) {
        super(parent);
    }

    public ConceptBuilderUnion addMember(ConceptBuilder member) {
        this.members.add(member);
    }

}
