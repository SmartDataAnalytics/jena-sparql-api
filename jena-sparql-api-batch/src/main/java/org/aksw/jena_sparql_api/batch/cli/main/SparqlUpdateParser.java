package org.aksw.jena_sparql_api.batch.cli.main;

import com.google.common.base.Function;
import com.hp.hpl.jena.update.UpdateRequest;

public interface SparqlUpdateParser
    extends Function<String, UpdateRequest>
{
}
