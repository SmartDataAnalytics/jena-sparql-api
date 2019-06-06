package org.aksw.jena_sparql_api.utils;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;

public class VarGeneratorBlacklist
    implements Generator<Var>
{
    private Generator<Var> generator;
    private Collection<?> blacklist;

    public VarGeneratorBlacklist(Generator<Var> generator, Collection<?> blacklist) {
        this.generator = generator;
        this.blacklist = blacklist;
    }

    @Override
    public VarGeneratorBlacklist clone() {
        Generator<Var> clone = generator.clone();
        VarGeneratorBlacklist result = new VarGeneratorBlacklist(clone, blacklist);
        return result;
    }

    @Override
    public Var next() {
        Var result;
        do {

            result = generator.next();

        } while(blacklist.contains(result));

        return result;
    }

    @Override
    public Var current() {
        Var result = generator.current();
        return result;
    }

    public static VarGeneratorBlacklist create(Collection<?> blacklist) {
        VarGeneratorBlacklist result = create("v", blacklist);
        return result;
    }

    public static VarGeneratorBlacklist create(String base, Collection<?> blacklist) {
        Generator<Var> generator = VarGeneratorImpl2.create(base);
        VarGeneratorBlacklist result = create(generator, blacklist);
        return result;
    }

    public static VarGeneratorBlacklist create(Generator<Var> generator, Collection<?> blacklist) {
        generator = generator == null ? VarGeneratorImpl2.create() : generator;
        VarGeneratorBlacklist result = new VarGeneratorBlacklist(generator, blacklist);
        return result;
    }

    @Override
    public String toString() {
        return "current: " + generator.current() + ", blacklist: " + blacklist;
    }
}
