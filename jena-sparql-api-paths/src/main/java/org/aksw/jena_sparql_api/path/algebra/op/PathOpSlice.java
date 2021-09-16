package org.aksw.jena_sparql_api.path.algebra.op;

/**
 * Only take a certain range of items from the sub op;
 *
 * rewrite(slice(op, limit, offset)) := { SELECT * { rewrite(op) } LIMIT limit OFFSET from }
 */
public class PathOpSlice
    extends PathOp1
{
    protected long offset;
    protected long limit;
}
