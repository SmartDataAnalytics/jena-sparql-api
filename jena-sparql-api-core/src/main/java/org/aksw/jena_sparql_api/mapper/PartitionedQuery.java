package org.aksw.jena_sparql_api.mapper;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

public interface PartitionedQuery {
    Query getQuery();
    List<Var> getPartitionVars();
}
