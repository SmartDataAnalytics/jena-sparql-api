package org.aksw.jena_sparql_api.algebra.utils;

import java.util.List;

import org.apache.jena.sparql.algebra.Op;

public interface OpCopyable {
    Op copy(List<Op> subOps);
    List<Op> getElements();
}
