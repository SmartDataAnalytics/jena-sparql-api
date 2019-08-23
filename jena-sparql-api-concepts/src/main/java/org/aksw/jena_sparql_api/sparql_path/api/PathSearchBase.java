package org.aksw.jena_sparql_api.sparql_path.api;

import java.util.function.Function;

import io.reactivex.Flowable;

public abstract class PathSearchBase<P>
	implements PathSearch<P>
{
	protected Long maxResults;
	protected Long maxLength;
	
	protected Function<Flowable<P>, Flowable<P>> filter;

	@Override
	public PathSearch<P> transformInternal(
			Function<Flowable<P>, Flowable<P>> f) {

		this.filter = this.filter == null ? f : this.filter.andThen(f);
		
		return this;
	}

	@Override
	public PathSearch<P> setMaxLength(Long maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	@Override
	public PathSearch<P> setMaxInternalResultsHint(Long maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	@Override
	public Long getMaxLength() {
		return maxLength;
	}

	@Override
	public Long getMaxInternalResultsHint() {
		return maxResults;
	}
	
	
//	@Override
//	public PathSearch<P> filter(Predicate<? super P> filter) {
//		this.filters.add(filter);
//	}
}
