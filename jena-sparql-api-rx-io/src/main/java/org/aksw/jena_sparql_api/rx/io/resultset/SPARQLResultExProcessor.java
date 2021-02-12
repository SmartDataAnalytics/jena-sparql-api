package org.aksw.jena_sparql_api.rx.io.resultset;

import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;

public interface SPARQLResultExProcessor
    extends SinkStreaming<SPARQLResultEx>, SPARQLResultExVisitor<Void>
{

}
