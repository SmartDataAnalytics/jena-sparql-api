package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.apache.jena.sparql.algebra.Op;

public class OpIndexer {



    public OpIndex create(Op op) {
        op = QueryToGraph.normalizeOp(op, false);

        // FILTER(?p = rdf:type && (?o = Foo) || (?o = Bar))
        // -> ?p becomes part of the conjunctive query, {{?o = Foo} { ?o = Bar}} becomes part of a parent DNF

        return null;
    }
}
