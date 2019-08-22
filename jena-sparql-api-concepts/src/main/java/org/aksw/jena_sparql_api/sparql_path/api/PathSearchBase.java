package org.aksw.jena_sparql_api.sparql_path.api;

public abstract class PathSearchBase<P>
	implements PathSearch<P>
{
	protected Long maxResults;
	protected Long maxLength;
	
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
}
