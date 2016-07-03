package org.aksw.isomorphism;

import java.util.List;

import com.google.common.collect.AbstractIterator;

public class BlockingCacheIterator<T>
    extends AbstractIterator<T>
{
    protected Cache<? extends List<? extends T>> cache;
    protected int offset;

    @Override
    public T computeNext() {
        List<? extends T> data = cache.getData();

        T result;
        for(;;) {
            if(offset < data.size()) {
                result = data.get(offset);
                ++offset;
                break;
            } else if(cache.isComplete()) {
                result = endOfData();
                break;
                //throw new IndexOutOfBoundsException();
            } else {
                try {
                    cache.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        return result;
    }

}
