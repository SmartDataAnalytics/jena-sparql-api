package org.aksw.jena_sparql_api.batch.step;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.mapper.MappedQuery;

import org.apache.jena.sparql.core.DatasetGraph;

public class HopItem {
    protected QueryExecutionFactory service;
    protected BinaryRelation via;
    protected List<MappedQuery<DatasetGraph>> mappedQuery;
}
