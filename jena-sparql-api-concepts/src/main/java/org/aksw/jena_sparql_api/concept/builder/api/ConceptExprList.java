package org.aksw.jena_sparql_api.concept.builder.api;

import java.util.List;

public interface ConceptExprList
    extends ConceptExpr
{
    boolean isUnionMode();
    ConceptExprList setUnionMode(boolean tf);

    List<ConceptExpr> getMembers();

    ConceptExprList addMember(ConceptExpr member);

}
