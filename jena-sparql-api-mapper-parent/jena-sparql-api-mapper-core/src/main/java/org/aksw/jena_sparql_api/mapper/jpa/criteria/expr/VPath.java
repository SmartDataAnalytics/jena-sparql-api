package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Path;

public interface VPath<T>
    extends VExpression<T>, Path<T>
{
    // The attribute name by which this path object was reached
    String getReachingAttributeName();

    <X> X accept(PathVisitor<X> visitor);
}
