package org.aksw.jena_sparql_api.core.utils;

import org.aksw.jena_sparql_api.utils.TripleUtils;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class F_TripleToBinding
    implements Function<Triple, Binding>
{
    @Override
    public Binding apply(Triple triple) {
        Binding result = TripleUtils.tripleToBinding(triple);
        return result;
    }

    public static final F_TripleToBinding fn = new F_TripleToBinding();
}
