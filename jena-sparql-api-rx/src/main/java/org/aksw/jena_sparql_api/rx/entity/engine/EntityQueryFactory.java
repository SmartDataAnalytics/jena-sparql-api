package org.aksw.jena_sparql_api.rx.entity.engine;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.rx.EntityBaseQuery;

public class EntityQueryFactory {
    public EntityBaseQuery createFromConcept(Concept concept) {
        EntityBaseQuery result = EntityBaseQuery.create(concept.getVar(), concept.asQuery());
        return result;
    }
}
