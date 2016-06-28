package org;

import java.util.Collection;
import java.util.Collections;

public class QuadPrefixes
{
    public static final QuadPrefixes ALWAYS_MATCHING = createAlwaysMatching();

    protected Quadlet<? extends Collection<String>> prefixes;

    protected boolean mayBeObjectLiteral;
    protected boolean mayBeObjectResource;

    public QuadPrefixes(Quadlet<? extends Collection<String>> prefixes,
            boolean mayBeObjectResource, boolean mayBeObjectLiteral) {
        super();
        this.prefixes = prefixes;
        this.mayBeObjectLiteral = mayBeObjectLiteral;
        this.mayBeObjectResource = mayBeObjectResource;
    }

    public Quadlet<? extends Collection<String>> getPrefixes() {
        return prefixes;
    }

    public boolean isMayBeObjectLiteral() {
        return mayBeObjectLiteral;
    }

    public boolean isMayBeObjectResource() {
        return mayBeObjectResource;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mayBeObjectLiteral ? 1231 : 1237);
        result = prime * result + (mayBeObjectResource ? 1231 : 1237);
        result = prime * result
                + ((prefixes == null) ? 0 : prefixes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuadPrefixes other = (QuadPrefixes) obj;
        if (mayBeObjectLiteral != other.mayBeObjectLiteral)
            return false;
        if (mayBeObjectResource != other.mayBeObjectResource)
            return false;
        if (prefixes == null) {
            if (other.prefixes != null)
                return false;
        } else if (!prefixes.equals(other.prefixes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PrefixDescription [prefixes=" + prefixes
                + ", mayBeObjectLiteral=" + mayBeObjectLiteral
                + ", mayBeObjectResource=" + mayBeObjectResource + "]";
    }


    public static QuadPrefixes createAlwaysMatching() {
        QuadPrefixes result = new QuadPrefixes(
                new Quadlet<>(
                        Collections.singleton(""),
                        Collections.singleton(""),
                        Collections.singleton(""),
                        Collections.singleton("")),
                true, true);
        return result;
    }
}
