package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;

public class OpExists
    extends OpRoleRestriction
{
    public OpExists(BinaryRelation role, Op filler) {
        super(role, filler);
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
