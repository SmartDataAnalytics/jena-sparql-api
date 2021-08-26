package org.aksw.jena_sparql_api.utils;

import org.apache.jena.irix.IRIxResolver;

public class IRIxResolverUtils {
    public static IRIxResolver newIRIxResolverAsGiven() {
        return IRIxResolver.create().noBase().resolve(false).allowRelative(true).build();
    }
}
