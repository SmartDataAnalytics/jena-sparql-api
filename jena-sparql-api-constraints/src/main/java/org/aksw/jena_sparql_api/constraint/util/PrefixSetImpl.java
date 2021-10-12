package org.aksw.jena_sparql_api.constraint.util;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

public class PrefixSetImpl
    extends AbstractSet<String>
    implements PrefixSet
{
    // protected Trie<String> trie;
    protected PatriciaTrie<String> trie;

    public PrefixSetImpl() {
        this(new PatriciaTrie<String>());
    }

    public static PrefixSetImpl create(String ...strs) {
        PrefixSetImpl result = new PrefixSetImpl();

        for (String str : strs) { result.add(str); }

        return result;
    }


    @Override
    public boolean add(String e) {
        boolean result = trie.containsKey(e);
        trie.put(e, e);
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = trie.containsKey(o);
        trie.remove(o);
        return result;
    }

    @Override
    public boolean contains(Object o) {
        return trie.containsKey(o);
    }

    public PrefixSetImpl(PatriciaTrie<String> trie) {
        super();
        this.trie = trie;
    }

    @Override
    public Iterator<String> iterator() {
        return trie.keySet().iterator();
    }

    @Override
    public int size() {
        return trie.size();
    }


    @Override
    public Set<String> getPrefixesOf(String str, boolean inclusive) {
        Set<String> result = trie.prefixMap(str).keySet();

        if (!inclusive) {
            result = Sets.difference(result, Collections.singleton(str));
        }

        return result;
    }


    public boolean intersect(PrefixSet other) {

        boolean result = false; // true on change
        // {http:, mailto:addr} {http://foo, mailto:}

        // Note: If we have prefixes Foo and FooBar, we keep FooBar, which is more restrictive.
        for(String s : other) {
            Set<String> ps = getPrefixesOf(s, false);
            if (!ps.isEmpty()) {
                removeAll(ps);
                add(s);
                result = true;
            }
        }

        // Remove all entries that do not have a prefix in the other set
        Iterator<String> it = iterator();
        while(it.hasNext()) {
            String s = it.next();
            Set<String> ps = other.getPrefixesOf(s, true);
            if(ps.isEmpty()) {
                it.remove();
                result = true;
            }
        }

        return result;
    }



    @Override
    public String toString() {
        return "PrefixSetImpl [trie=" + trie + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((trie == null) ? 0 : trie.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrefixSetImpl other = (PrefixSetImpl) obj;
        if (trie == null) {
            if (other.trie != null)
                return false;
        } else if (!trie.equals(other.trie))
            return false;
        return true;
    }



}
