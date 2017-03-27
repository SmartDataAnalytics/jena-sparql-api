package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FunctionMemoize<I, O>
	implements Function<I, O>
{
	protected Map<I, O> cache;
	protected Function<I, O> delegate;

	public FunctionMemoize(Function<I, O> delegate) {
		this(delegate, new HashMap<>());
	}

	public FunctionMemoize(Function<I, O> delegate, Map<I, O> cache) {
		super();
		this.cache = new HashMap<>();
		this.delegate = delegate;
	}

	public Map<I, O> getCache() {
		return cache;
	}

	public void setCache(Map<I, O> cache) {
		this.cache = cache;
	}

	public Function<I, O> getDelegate() {
		return delegate;
	}

	public void setDelegate(Function<I, O> delegate) {
		this.delegate = delegate;
	}

	@Override
	public O apply(I t) {
		O result = cache.computeIfAbsent(t, delegate);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cache == null) ? 0 : cache.hashCode());
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		FunctionMemoize<?, ?> other = (FunctionMemoize<?, ?>) obj;
		if (cache == null) {
			if (other.cache != null)
				return false;
		} else if (!cache.equals(other.cache))
			return false;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionMemoize [cache=" + cache + ", delegate=" + delegate + "]";
	}
}
