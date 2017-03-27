package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jena_sparql_api.concepts.Relation;

public abstract class OpRoleRestriction
    extends Op1 // TODO use the appropriate one
{
    protected Relation role;

    public OpRoleRestriction(Relation role, Op filler) {
        super(filler);
        this.role = role;
        //this.filler = filler;
    }

    public Op getFiller()
    {
        return subOp;
    }

    public Relation getRole() {
        return role;
    }
}
