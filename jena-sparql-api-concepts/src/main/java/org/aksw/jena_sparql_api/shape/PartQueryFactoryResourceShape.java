package org.aksw.jena_sparql_api.shape;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.stmt.PartQueryFactory;

import org.apache.jena.query.Query;

public class PartQueryFactoryResourceShape
    implements PartQueryFactory
{
    protected ResourceShape shape;

    @Override
    public Query createQuery(Concept filter) {
        return null;
        //shape.createMappedConcept2(resourceShape, filter);
    }

}
