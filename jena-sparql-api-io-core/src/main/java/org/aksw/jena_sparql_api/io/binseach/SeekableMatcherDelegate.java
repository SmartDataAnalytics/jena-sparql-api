package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class SeekableMatcherDelegate 
	implements SeekableMatcher
{
	protected SeekableMatcher delegate;
	
	public SeekableMatcherDelegate(SeekableMatcher delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean isForward() {
		return delegate.isForward();
	}

	@Override
	public void resetState() {
		delegate.resetState();
	}

	@Override
	public boolean find(Seekable seekable) throws IOException {
		return delegate.find(seekable);
	}

}
