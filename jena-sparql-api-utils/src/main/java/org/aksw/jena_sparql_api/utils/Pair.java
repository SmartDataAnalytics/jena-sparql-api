package org.aksw.jena_sparql_api.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


/**
 * A pair is a collection having 2 entries of same type
 *
 * Note: For a pair with two different types, use Map.SimpleEntry
 *
 * @author raven
 *
 * @param <T>
 */
public class Pair<T>
    //extends AbstractList<T>
    implements Entry<T, T>, Iterable<T>, Serializable
{
    private static final long serialVersionUID = 7898871427844686243L;

    protected T key;
    protected T value;

    public Pair(Entry<? extends T, ? extends T> entry) {
        super();
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public Pair(T key, T value) {
        super();
        this.key = key;
        this.value = value;
    }


    @Override
    public Iterator<T> iterator() {
        List<T> tmp = Arrays.asList(key, value);
        return tmp.iterator();
    }

//    @Override
//    public void add(int index, T element) {
//        set(index, element);
//    }

    //@Override
    public T set(int index, T element) {
        switch(index) {
        case 0: key = element; break;
        case 1: value = element; break;
        default: throw new IndexOutOfBoundsException("Requested index: " + index);
        }
        return element;
    }

    //@Override
    public T get(int index) {
        T result;
        switch(index) {
        case 0: result = key; break;
        case 1: result = value; break;
        default: throw new IndexOutOfBoundsException("Requested index: " + index);
        }
        return result;
    }

    //@Override
    public int size() {
        return 2;
    }

    @Override
    public T getKey() {
        return key;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        Pair<?> other = (Pair<?>) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Pair [key=" + key + ", value=" + value + "]";
    }



}
