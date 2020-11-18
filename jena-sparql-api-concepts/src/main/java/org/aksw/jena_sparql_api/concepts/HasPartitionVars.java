package org.aksw.jena_sparql_api.concepts;

import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface HasPartitionVars {
    List<Var> getPartitionVars();
}
