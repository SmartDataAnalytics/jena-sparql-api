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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agg == null) ? 0 : agg.hashCode());
        result = prime * result + ((concept == null) ? 0 : concept.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MappedConcept other = (MappedConcept) obj;
        if (agg == null) {
            if (other.agg != null)
                return false;
        } else if (!agg.equals(other.agg))
            return false;
        if (concept == null) {
            if (other.concept != null)
                return false;
        } else if (!concept.equals(other.concept))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MappedConcept [concept=" + concept + ", agg=" + agg + "]";
    }

}

