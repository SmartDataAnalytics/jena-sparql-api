package org.aksw.isomorphism;

import java.util.List;

import com.google.common.collect.AbstractIterator;

public class BlockingCacheIterator<T>
    extends AbstractIterator<T>
{
    protected Cache<T> cache;
    protected int offset;

    @Override
    public T computeNext() {
        List<T> data = cache.getData();

        T result;
        for(;;) {
            if(offset < data.size()) {
                result = data.get(offset);
                ++offset;
            } else if(cache.isComplete()) {
                result = endOfData();
                //throw new IndexOutOfBoundsException();
            } else {
                // Wait for data to become available
                cache.wait();
            }
        }

        return result;
    }

}
