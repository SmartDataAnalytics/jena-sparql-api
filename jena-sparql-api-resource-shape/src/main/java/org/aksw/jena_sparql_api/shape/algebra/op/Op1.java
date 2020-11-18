package org.aksw.jena_sparql_api.shape.algebra.op;

public abstract class
    Op1 implements Op
{
    protected Op subOp;

    public Op1(Op subOp) {
        super();
        this.subOp = subOp;
    }

    public Op getSubOp() {
        return subOp;
    }
}
