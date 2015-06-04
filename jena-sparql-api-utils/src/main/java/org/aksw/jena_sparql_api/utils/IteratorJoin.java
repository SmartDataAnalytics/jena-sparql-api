package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.CartesianProductIterator;
import org.aksw.commons.collections.PrefetchIterator;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class IteratorJoin<K>
    extends PrefetchIterator<Binding>
{
    private Iterator<K> itKey;

    private Multimap<K, Binding> a;
    private Multimap<K, Binding> b;


    public IteratorJoin(Iterator<K> itKey, Multimap<K, Binding> a, Multimap<K, Binding> b) {
        this.itKey = itKey;
        this.a = a;
        this.b = b;
    }

    @Override
    protected Iterator<Binding> prefetch() throws Exception {

        Iterator<Binding> result = null;

        while(itKey.hasNext()) {
            K key = itKey.next();

            Collection<Binding> as = a.get(key);
            Collection<Binding> bs = b.get(key);

            if(as.isEmpty() || bs.isEmpty()) {
                continue;
            }

            Iterator<List<Binding>> tmp = new CartesianProductIterator<Binding>(as, bs);

            result = new IteratorBindingJoin(tmp);

            break;
        }

        return result;
    }

}