package org.aksw.jena_sparql_api_sparql_path2;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


/**
 * A pair is a collection having 2 entries of same type
 *
 * @author raven
 *
 * @param <T>
 */
public class Pair<T>
    extends AbstractList<T>
    implements Entry<T, T>, Serializable
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

    @Override
    public T get(int index) {
        T result;
        switch(index) {
        case 0: result = key; break;
        case 1: result = value; break;
        default: throw new IndexOutOfBoundsException();
        }
        return result;
    }

    @Override
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
}
