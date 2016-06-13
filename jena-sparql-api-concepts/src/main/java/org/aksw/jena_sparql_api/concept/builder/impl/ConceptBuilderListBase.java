package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilderList;
import org.aksw.jena_sparql_api.concept.builder.api.RestrictionBuilder;

public abstract class ConceptBuilderListBase
    extends ConceptBuilderBase
    implements ConceptBuilderList
{
    protected List<ConceptBuilder> members;

    public ConceptBuilderListBase(RestrictionBuilder parent) {
        this(parent, new ArrayList<>());
    }

    public ConceptBuilderListBase(RestrictionBuilder parent, List<ConceptBuilder> members) {
        super(parent);
        this.members = members;
    }

    public List<ConceptBuilder> getMembers() {
        return members;
    }
}
