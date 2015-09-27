package org.aksw.jena_sparql_api.batch.cli.main;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.Query;

public interface SparqlQueryParser
    extends Function<String, Query>
{
}
