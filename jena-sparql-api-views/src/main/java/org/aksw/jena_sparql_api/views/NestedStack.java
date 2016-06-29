package org.aksw.jena_sparql_api.views;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NestedStack<T>
    extends AbstractCollection<T>
{
    protected NestedStack<T> parent;
    protected T value;

    // The depth corresponds to the size of the collection
    protected int size;

    public NestedStack(NestedStack<T> parent, T value) {
        super();
        this.parent = parent;
        this.value = value;

        size = parent == null ? 1 : parent.size;
    }

    public NestedStack<T> getParent() {
        return parent;
    }

    public T getValue() {
        return value;
    }


    public List<T> asList() {
        List<T> result = new ArrayList<T>(this);
//
//        NestedStack<T> current = this;
//        while(current != null) {
//            result.add(current.getValue());
//            current = current.parent;
//        }
//
        Collections.reverse(result);

        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = new NestedStackIterator<T>(this);
        return result;
    }

    @Override
    public int size() {
        return size;
    }
}
