package org.aksw.jena_sparql_api.io.endpoint;

import io.reactivex.Single;

public class DestinationFilter
	implements Destination
{
	protected FilterConfig filter;
	
	public DestinationFilter(FilterConfig filter) {
		super();
		this.filter = filter;
	}
	
	public FilterConfig getFilter() {
		return filter;
	}

	@Override
	public Single<InputStreamSupplier> prepareStream() {
		//filter.execStream()
		// TODO Should we allow obtaining an input stream directly from the filter -
		// or should there be some other destination in between?
		// In the former case, we need to align the API w.r.t. InputStream and InputStreamSupplier
		//throw new RuntimeException("Not implemented");
		Single<InputStreamSupplier> result = filter.execStream();
		return result;
	}

	@Override
	public FilterConfig transferTo(FilterEngine engine) {
		FilterConfig result = engine.forInput(filter);
		return result;
	}
	
}
