package org.aksw.jena_sparql_api.views.index;

import org.apache.jena.sparql.algebra.Op;

public interface SparqlViewMatcherSystem {
    Op rewriteQuery(Op queryOp);

}
