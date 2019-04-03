package org.aksw.jena_sparql_api.utils;

import com.google.common.collect.ComparisonChain;

public class CountInfo
	implements Comparable<CountInfo>
{
    private long count;
    private boolean hasMoreItems;
    private Long itemLimit;

    
    public CountInfo(long count, boolean hasMoreItems, Long itemLimit) {
        this.count = count;
        this.hasMoreItems = hasMoreItems;
        this.itemLimit = itemLimit;
    }

    public long getCount() {
        return count;
    }

    public boolean isHasMoreItems() {
        return hasMoreItems;
    }

    public long getItemLimit() {
        return itemLimit;
    }

    @Override
    public String toString() {
        return "CountInfo [count=" + count + ", hasMoreItems=" + hasMoreItems
                + ", itemLimit=" + itemLimit + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (count ^ (count >>> 32));
        result = prime * result + (hasMoreItems ? 1231 : 1237);
        result = prime * result
                + ((itemLimit == null) ? 0 : itemLimit.hashCode());
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
        CountInfo other = (CountInfo) obj;
        if (count != other.count)
            return false;
        if (hasMoreItems != other.hasMoreItems)
            return false;
        if (itemLimit == null) {
            if (other.itemLimit != null)
                return false;
        } else if (!itemLimit.equals(other.itemLimit))
            return false;
        return true;
    }

    /**
     * All exact counts are sorted first.
     * Item limit is not used in comparison. 
     * 
     */
	@Override
	public int compareTo(CountInfo o) {
		int result = ComparisonChain.start()
			.compareFalseFirst(hasMoreItems, o.hasMoreItems)
			.compare(count, o.count)
			.compareTrueFirst(itemLimit == null, o.itemLimit == null)
			.result();

		return result;
	}
    
    
}
