package org.aksw.jena_sparql_api.constraint.util;

import java.util.Set;

public interface PrefixSet
    extends Set<String>
{
    /**
     * Return the set of prefixes of str
     *
     * @param str
     * @param inclusive True if str should be matched as well
     * @return
     */
    Set<String> getPrefixesOf(String str, boolean inclusive);


//    String longestMatch(String key);
//    String shortestMatch(String key);

    /**
     * (1) Replace shorter (more generic) prefixes in this with longer (more specific) ones in other
     * when the former is a prefix of the latter.
     *
     * (2) Replace prefixes in this with shorter ones from other when the latter is a prefix for the former.
     *
     * @param other
     * @return True when the operation modified this.
     */
    boolean intersect(PrefixSet other);

}
