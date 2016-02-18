package org.aksw.jena_sparql_api_sparql_path2;

public class Triplet<V, E> {
    protected V subject;
    protected E predicate;
    protected V object;

    public static <V, E> Triplet<V, E> swap(Triplet<V, E> t) {
        Triplet<V, E> result = new Triplet<>(t.getObject(), t.getPredicate(), t.getSubject());
        return result;
    }

    public Triplet(V subject, E predicate, V object) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public V getSubject() {
        return subject;
    }

    public E getPredicate() {
        return predicate;
    }

    public V getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result
                + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
        Triplet other = (Triplet) obj;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PathTriple [subject=" + subject + ", predicate=" + predicate
                + ", object=" + object + "]";
    }
}
