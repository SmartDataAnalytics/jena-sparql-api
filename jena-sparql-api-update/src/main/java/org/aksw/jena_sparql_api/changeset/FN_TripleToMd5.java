package org.aksw.jena_sparql_api.changeset;

import org.aksw.jena_sparql_api.utils.TripleUtils;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Triple;

public class FN_TripleToMd5
    implements Function<Triple, String>
{
    @Override
    public String apply(Triple triple) {
        String result = TripleUtils.md5sum(triple);
        return result;
    }

    public static final FN_TripleToMd5 fn = new FN_TripleToMd5();
}