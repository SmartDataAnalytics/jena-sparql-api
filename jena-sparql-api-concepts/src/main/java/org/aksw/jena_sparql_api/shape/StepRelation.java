package org.aksw.jena_sparql_api.shape;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;

/**
 * Combine an expression with a direction
 * @author raven
 *
 */
public class StepRelation {
    private final BinaryRelation relation;
    private final boolean isInverse;
    
    public StepRelation(BinaryRelation expr, boolean isInverse) {
        super();
        this.relation = expr;
        this.isInverse = isInverse;
    }

    public BinaryRelation getRelation() {
        return relation;
    }

    public boolean isInverse() {
        return isInverse;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((relation == null) ? 0 : relation.hashCode());
        result = prime * result + (isInverse ? 1231 : 1237);
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
        StepRelation other = (StepRelation) obj;
        if (relation == null) {
            if (other.relation != null)
                return false;
        } else if (!relation.equals(other.relation))
            return false;
        if (isInverse != other.isInverse)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StepRelation [Relation=" + relation + ", isInverse=" + isInverse + "]";
    }
}