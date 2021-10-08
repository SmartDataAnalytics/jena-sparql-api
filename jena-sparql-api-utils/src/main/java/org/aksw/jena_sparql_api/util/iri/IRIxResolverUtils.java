package org.aksw.jena_sparql_api.util.iri;

import org.apache.jena.irix.IRIProviderJDK;
import org.apache.jena.irix.IRIProviderJenaIRI.IRIxJena;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;

public class IRIxResolverUtils {
    public static IRIxResolver newIRIxResolverAsGiven() {
        // return IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
        return newIRIxResolverAsGiven(null);
    }

    /**
     * Create a resolver that can resolve against a baseIri "as given" -
     * the base Iri may be null or a relative IRI
     *
     * e.g.
     *   base("foo/").resolve("bar) -&gt; "foo/bar"
     *   TODO Right now this method requires trailing slashes - should be allow base("foo").resolve("bar) -&gt; "foobar" ?
     *
     * For this purpose {@link IRIxJena#create(String)} is used.
     */
    public static IRIxResolver newIRIxResolverAsGiven(String baseIri) {
        IRIxResolver result = baseIri == null
                ? IRIxResolver.create().noBase().resolve(false).allowRelative(true).build()
                : IRIxResolver.create().base(baseIri).resolve(true).allowRelative(true).build();

        // "Fix" the resolver - we want to resolve relative base URLs "as given"
        if(baseIri != null) {
            // IRIx base = IRIxJena.create(baseIri);
            IRIx base = newIRIxAsGiven(baseIri);
            result = result.resetBase(base);
        }

        return result;
    }


    public static IRIx newIRIxAsGiven(String baseIri) {
        IRIx result = new IRIProviderJDK().create(baseIri);
        return result;
    }

}
