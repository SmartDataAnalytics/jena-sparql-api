package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public abstract class MapPaginatorSparqlQueryBase<K, V>
    extends ListPaginatorSparqlQueryBase<Entry<K, V>>
    implements MapPaginator<K, V>
{
    public MapPaginatorSparqlQueryBase(SparqlQueryConnection qef, Concept filterConcept, boolean isLeftJoin) {
        super(qef, filterConcept, isLeftJoin);
    }
}
