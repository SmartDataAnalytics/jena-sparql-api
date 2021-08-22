package org.aksw.jena_sparql_api.utils;

import java.lang.reflect.Field;

import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Prologue;

public class PrologueUtils {

    /** Configure the target prologue by copying the config from source and setting the baseURI */
    public static Prologue configure(Prologue target, Prologue source, String baseURI) {
        PrologueUtils.copy(target, source);
        setBaseURI(target, baseURI);

        return target;
    }

    /** Set the baseURI by resolving it using the target's resolver */
    public static Prologue setBaseURI(Prologue target, String baseURI) {
        if (baseURI != null) {
            IRIxResolver resolver = target.getResolver();

            if (resolver != null) {
                target.setBase(resolver.resolve(baseURI));
            } else {
                target.setBaseURI(baseURI);
            }
        }

        return target;
    }

    /** Copy the state from source into target. Appends namespaces and copies the resolver. */
    public static Prologue copy(Prologue target, Prologue source) {
        copyResolver(target, source);
        target.getPrefixMapping().setNsPrefixes(source.getPrefixMapping());
        return target;
    }

    /** Set the target's resolver to that of source */
    public static Prologue copyResolver(Prologue target, Prologue source) {
        if (source.explicitlySetBaseURI()) {
            target.setBase(source.getBase());
        } else {
            setResolver(target, source.getResolver());
        }

        return target;
    }

    /** The missing counterpart to Prologue.getResolver in Jena 4.0.0 */
    public static Prologue setResolver(Prologue prologue, IRIxResolver resolver) {
        Field field;
        try {
            field = Prologue.class.getDeclaredField("resolver");
            field.setAccessible(true);
            field.set(prologue, resolver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return prologue;
    }
}
