package org.aksw.jena_sparql_api.batch.step;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.mapper.MappedQuery;

import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class HopItem {
    protected QueryExecutionFactory service;
    protected Relation via;
    protected List<MappedQuery<DatasetGraph>> mappedQuery;
}
