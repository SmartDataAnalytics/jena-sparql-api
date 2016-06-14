package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprBase;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprList;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprVisitor;

public class ConceptExprListImpl
    extends ConceptExprBase
    implements ConceptExprList
{
    protected boolean isUnionMode;
    protected List<ConceptExpr> members;

    public ConceptExprListImpl() {
        this(false, new ArrayList<>());
    }

    public ConceptExprListImpl(boolean isUnionMode, List<ConceptExpr> members) {
        super();
        this.isUnionMode = isUnionMode;
        this.members = members;
    }

    public boolean isUnionMode() {
        return isUnionMode;
    }

    public ConceptExprListImpl setUnionMode(boolean isUnionMode) {
        this.isUnionMode = isUnionMode;
        return this;
    }

    @Override
    public ConceptExprList addMember(ConceptExpr member) {
        members.add(member);
        return this;
    }

    public List<ConceptExpr> getMembers() {
        return members;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public ConceptExprList asList() {
        return this;
    }

    @Override
    public <T> T accept(ConceptExprVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
