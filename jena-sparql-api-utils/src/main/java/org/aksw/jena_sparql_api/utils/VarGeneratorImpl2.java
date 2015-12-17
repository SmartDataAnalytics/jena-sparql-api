package org.aksw.jena_sparql_api.utils;

import com.hp.hpl.jena.sparql.core.Var;

/**
 * Wrap Jena SDB's Generator to yield objects of type Var
 *
 * @author raven
 *
 */
public class VarGeneratorImpl2
    implements Generator<Var>
{
    protected String base;
    protected int nextId;
    protected Var current = null;

    public VarGeneratorImpl2(String base, int nextId) {
        this.base = base;
        this.nextId = nextId;
    }

    @Override
    public VarGeneratorImpl2 clone() {
        VarGeneratorImpl2 result = new VarGeneratorImpl2(base, nextId);
        return result;
    }

    @Override
    public Var next() {
        String varName = base + "_" + nextId;
        ++nextId;

        current = Var.alloc(varName);
        return current;
    }


    @Override
    public Var current() {
        return current;
    }

    public static VarGeneratorImpl2 create(String base) {
        VarGeneratorImpl2 result = new VarGeneratorImpl2(base, 1);
        return result;
    }
}
