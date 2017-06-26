package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.From;

public interface VFrom<Z, X>
    extends From<Z, X>, VPath<X>
{

}
