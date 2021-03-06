package org.aksw.jena_sparql_api.utils;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.AbstractIterator;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.engine.binding.Binding;

public class IteratorBindingJoin
    extends AbstractIterator<Binding>
{
    private Iterator<List<Binding>> bindings;

    public IteratorBindingJoin(Iterator<List<Binding>> bindings) {
        this.bindings = bindings;
    }

    @Override
    protected Binding computeNext() {
        while(bindings.hasNext()) {
            List<Binding> cand = bindings.next();

            Binding r = null;
            for(Binding b : cand) {
                if(r == null) {
                    r = b;
                } else {
                    boolean isCompatible = Algebra.compatible(r, b);
                    if(isCompatible) {
                        r = Algebra.merge(r, b);
                    } else {
                        continue;
                    }
                }
            }

            return r;
        }

        return endOfData();
    }
}