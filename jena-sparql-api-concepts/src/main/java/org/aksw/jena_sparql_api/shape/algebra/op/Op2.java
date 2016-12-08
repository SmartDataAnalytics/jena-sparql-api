package org.aksw.jena_sparql_api.shape.algebra.op;

public abstract class Op2
    implements Op
{
    protected Op left;
    protected Op right;

    public Op2(Op left, Op right) {
        super();
        this.left = left;
        this.right = right;
    }

    public Op getLeft() {
        return left;
    }

    public Op getRight() {
        return right;
    }
}
