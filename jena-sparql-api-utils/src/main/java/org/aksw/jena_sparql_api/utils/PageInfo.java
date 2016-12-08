package org.aksw.jena_sparql_api.utils;

import com.google.common.collect.Range;

public class PageInfo<T extends Comparable<T>> {
    protected Range<T> outerItemRange;
    protected Range<T> innerItemRange;
    
    public PageInfo(Range<T> outerItemRange, Range<T> innerItemRange) {
        super();
        this.outerItemRange = outerItemRange;
        this.innerItemRange = innerItemRange;
    }

    public Range<T> getOuterItemRange() {
        return outerItemRange;
    }

    public Range<T> getInnerItemRange() {
        return innerItemRange;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((innerItemRange == null) ? 0 : innerItemRange.hashCode());
        result = prime * result
                + ((outerItemRange == null) ? 0 : outerItemRange.hashCode());
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
        PageInfo<?> other = (PageInfo<?>) obj;
        if (innerItemRange == null) {
            if (other.innerItemRange != null)
                return false;
        } else if (!innerItemRange.equals(other.innerItemRange))
            return false;
        if (outerItemRange == null) {
            if (other.outerItemRange != null)
                return false;
        } else if (!outerItemRange.equals(other.outerItemRange))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PageInfo [outerItemRange=" + outerItemRange
                + ", innerItemRange=" + innerItemRange + "]";
    }
}
