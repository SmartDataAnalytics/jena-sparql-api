package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.algebra.Op;

public interface ViewCacheIndexer {

    //@Override
    QueryExecution createQueryExecution(Op indexPattern, Query query);

}