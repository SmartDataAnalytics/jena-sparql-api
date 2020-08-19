package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;

public abstract class OpRoleRestriction
    extends Op1 // TODO use the appropriate one
{
    protected BinaryRelation role;

    public OpRoleRestriction(BinaryRelation role, Op filler) {
        super(filler);
        this.role = role;
        //this.filler = filler;
    }

    public Op getFiller()
    {
        return subOp;
    }

    public BinaryRelation getRole() {
        return role;
    }
}
