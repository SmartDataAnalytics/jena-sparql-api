package org.aksw.jena_sparql_api.io.endpoint;

import com.google.common.io.ByteSource;

import io.reactivex.Single;

public class DestinationFromByteSource
	implements Destination
{
	protected ByteSource byteSource;
	
	public DestinationFromByteSource(ByteSource byteSource) {
		super();
		this.byteSource = byteSource;
	}
	
	@Override
	public Single<InputStreamSupplier> prepareStream() {
		return Single.just(byteSource::openStream);
	}

	@Override
	public FilterConfig transferTo(FilterEngine engine) {
		return engine.forInput(byteSource::openStream);
	}

}
