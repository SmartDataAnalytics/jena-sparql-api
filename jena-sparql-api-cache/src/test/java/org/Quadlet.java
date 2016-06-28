package org;

import java.util.AbstractList;

public class Quadlet<V>
    extends AbstractList<V>
{
    protected V g;
    protected V s;
    protected V p;
    protected V o;

    public Quadlet(V g, V s, V p, V o) {
        super();
        this.g = g;
        this.s = s;
        this.p = p;
        this.o = o;
    }

    public V getGraph() {
        return g;
    }

    public V getSubject() {
        return s;
    }

    public V getPredicate() {
        return p;
    }

    public V getObject() {
        return o;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((g == null) ? 0 : g.hashCode());
        result = prime * result + ((o == null) ? 0 : o.hashCode());
        result = prime * result + ((p == null) ? 0 : p.hashCode());
        result = prime * result + ((s == null) ? 0 : s.hashCode());
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
        Quadlet<?> other = (Quadlet<?>) obj;
        if (g == null) {
            if (other.g != null)
                return false;
        } else if (!g.equals(other.g))
            return false;
        if (o == null) {
            if (other.o != null)
                return false;
        } else if (!o.equals(other.o))
            return false;
        if (p == null) {
            if (other.p != null)
                return false;
        } else if (!p.equals(other.p))
            return false;
        if (s == null) {
            if (other.s != null)
                return false;
        } else if (!s.equals(other.s))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Quadlet [g=" + g + ", s=" + s + ", p=" + p + ", o=" + o + "]";
    }

    @Override
    public V get(int index) {
        V result;
        switch(index) {
        case 0: result = g; break;
        case 1: result = s; break;
        case 2: result = p; break;
        case 3: result = o; break;
        default: throw new IndexOutOfBoundsException();
        }

        return result;
    }

    @Override
    public int size() {
        return 4;
    }
}
