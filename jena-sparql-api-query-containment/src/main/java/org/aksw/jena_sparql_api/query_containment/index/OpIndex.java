package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.collections.trees.Tree;
import org.apache.jena.sparql.algebra.Op;

public class OpIndex {
    protected Op originalOp;
    protected Op normalizedOp;


    protected Tree<Op> normalizedOpTree;
    //protected Map<Op, G> opToGraph;


}
