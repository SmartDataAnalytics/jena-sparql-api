package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class FunctionDiffFromBindings
    implements Function<Iterable<? extends Binding>, Diff<Set<Quad>>>
{
    private Diff<? extends Iterable<Quad>> quadDiff;

    public FunctionDiffFromBindings(Diff<? extends Iterable<Quad>> quadDiff) {
        this.quadDiff = quadDiff;
    }

    @Override
    public Diff<Set<Quad>> apply(Iterable<? extends Binding> bindings) {
        Diff<Set<Quad>> result = UpdateUtils.buildDiff(bindings, quadDiff);
        return result;
    }


    public static FunctionDiffFromBindings create(Diff<? extends Iterable<Quad>> quadDiff) {
        FunctionDiffFromBindings result = new FunctionDiffFromBindings(quadDiff);
        return result;
    }
}
