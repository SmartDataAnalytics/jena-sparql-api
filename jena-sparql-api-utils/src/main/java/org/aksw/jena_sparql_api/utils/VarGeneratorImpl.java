package org.aksw.jena_sparql_api.utils;

import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * Wrap Jena SDB's Generator to yield objects of type Var
 *
 * @author raven
 *
 */
public class VarGeneratorImpl
    implements Generator<Var>
{
    private com.hp.hpl.jena.sdb.core.Generator gen;

    public VarGeneratorImpl(com.hp.hpl.jena.sdb.core.Generator gen) {
        this.gen = gen;
    }

    @Override
    public VarGeneratorImpl clone() {
        String c = gen.current();
        com.hp.hpl.jena.sdb.core.Generator g = Gensym.create(c);
        VarGeneratorImpl result = new VarGeneratorImpl(g);
        return result;
    }

    @Override
    public Var next() {
        String varName = gen.next();
        Var result = Var.alloc(varName);
        return result;
    }


    @Override
    public Var current() {
        String varName = gen.current();
        Var result = Var.alloc(varName);
        return result;
    }

    public static VarGeneratorImpl create(String base) {
        Gensym gen = Gensym.create(base);
        VarGeneratorImpl result = new VarGeneratorImpl(gen);
        return result;
    }
}
