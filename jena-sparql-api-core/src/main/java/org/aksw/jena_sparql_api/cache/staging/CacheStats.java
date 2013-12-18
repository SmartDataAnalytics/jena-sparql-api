package org.aksw.jena_sparql_api.cache.staging;

public class CacheStats {
	private int hitCount = 0;
	private int missCount = 0;
	
	public CacheStats(int hitCount, int missCount) {
		this.hitCount = hitCount;
		this.missCount = missCount;
	}
	
	public int getHitCount() {
		return this.hitCount;
	}
	
	public int getMissCount() {
		return this.missCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hitCount;
		result = prime * result + missCount;
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
		CacheStats other = (CacheStats) obj;
		if (hitCount != other.hitCount)
			return false;
		if (missCount != other.missCount)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CacheStats [hitCount=" + hitCount + ", missCount=" + missCount
				+ "]";
	}
}