package org.aksw.jena_sparql_api.views.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import org.aksw.jena_sparql_api.utils.model.Quadlet;

public class QuadPrefixes
{
    public static final QuadPrefixes ALWAYS_MATCHING = createAlwaysMatching();

    protected Quadlet<? extends NavigableSet<String>> prefixes;

    protected boolean mayBeObjectLiteral;
    protected boolean mayBeObjectResource;

    public QuadPrefixes(Quadlet<? extends NavigableSet<String>> prefixes,
            boolean mayBeObjectResource, boolean mayBeObjectLiteral) {
        super();
        this.prefixes = prefixes;
        this.mayBeObjectLiteral = mayBeObjectLiteral;
        this.mayBeObjectResource = mayBeObjectResource;
    }

    public Quadlet<? extends NavigableSet<String>> getPrefixes() {
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
        NavigableSet<String> x = new TreeSet<String>(Collections.singleton(""));

        QuadPrefixes result = new QuadPrefixes(new Quadlet<>(x, x, x, x), true, true);
        return result;
    }

    public static QuadPrefixes intersect(QuadPrefixes a, QuadPrefixes b) {
        QuadPrefixes result = applyOp(a, b, CandidateViewSelectorImpl::intersectPrefixes, Boolean::logicalAnd);
        return result;
    }

    public static QuadPrefixes union(QuadPrefixes a, QuadPrefixes b) {
        QuadPrefixes result = applyOp(a, b, CandidateViewSelectorImpl::unionPrefixes, Boolean::logicalOr);
        return result;
    }

    public static QuadPrefixes applyOp(QuadPrefixes a, QuadPrefixes b, BinaryOperator<NavigableSet<String>> prefixOp, BinaryOperator<Boolean> booleanOp) {
        Quadlet<? extends NavigableSet<String>> pa = a.getPrefixes();
        Quadlet<? extends NavigableSet<String>> pb = b.getPrefixes();

        int n = pa.size();
        List<NavigableSet<String>> components = new ArrayList<>(4);
        for(int i = 0; i < n; ++i) {
            NavigableSet<String> pas = pa.get(i);
            NavigableSet<String> pbs = pb.get(i);

            NavigableSet<String> c = prefixOp.apply(pas, pbs);
            components.add(c);
        }
        Quadlet<NavigableSet<String>> quadlet = new Quadlet<>(components);

        boolean mayBeResource = booleanOp.apply(a.isMayBeObjectResource(), b.isMayBeObjectResource());
        boolean mayBeLiteral = booleanOp.apply(a.isMayBeObjectLiteral(), b.isMayBeObjectLiteral());

        QuadPrefixes result = new QuadPrefixes(quadlet, mayBeResource, mayBeLiteral);
        return result;
    }

}
