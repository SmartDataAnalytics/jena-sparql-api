package org.aksw.jena_sparql_api.shape;

import org.aksw.jena_sparql_api.concepts.Concept;

public class OpDir2Base {
    private boolean isInverse;
    private Concept predicateConcept;
    
    public OpDir2Base(boolean isInverse, Concept predicateConcept) {
        super();
        this.isInverse = isInverse;
        this.predicateConcept = predicateConcept;
    }

    public boolean isInverse() {
        return isInverse;
    }

    public Concept getPredicateConcept() {
        return predicateConcept;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isInverse ? 1231 : 1237);
        result = prime * result + ((predicateConcept == null) ? 0
                : predicateConcept.hashCode());
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
        OpDir2Base other = (OpDir2Base) obj;
        if (isInverse != other.isInverse)
            return false;
        if (predicateConcept == null) {
            if (other.predicateConcept != null)
                return false;
        } else if (!predicateConcept.equals(other.predicateConcept))
            return false;
        return true;
    }
}
