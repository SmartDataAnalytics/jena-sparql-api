package org.aksw.jena_sparql_api.mapper;

import org.aksw.jena_sparql_api.concepts.Concept;

public class MappedConcept<T> {

    // Grouping is performed by the concepts' variable
    private Concept concept;
    private Agg<T> agg;
    
    public MappedConcept(Concept concept, Agg<T> agg) {
        super();
        this.concept = concept;
        this.agg = agg;
    }
    
    public Concept getConcept() {
        return concept;
    }

    public Agg<T> getAggregator() {
        return agg;
    }

    public static <T> MappedConcept<T> create(Concept concept, Agg<T> agg) {
        MappedConcept<T> result = new MappedConcept<T>(concept, agg);
        return result;
    }
}

