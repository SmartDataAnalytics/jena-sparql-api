package org.aksw.jena_sparql_api.geo;

import org.aksw.jena_sparql_api.sparql_path.core.domain.Concept;

import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;

public class MappedConcept {

    // Grouping is performed by the concepts' variable
    private Concept concept;
    private Aggregator aggregator;
    
    public MappedConcept(Concept concept, Aggregator aggregator) {
        super();
        this.concept = concept;
        this.aggregator = aggregator;
    }
    
    public Concept getConcept() {
        return concept;
    }

    public Aggregator getAggregator() {
        return aggregator;
    }
}

