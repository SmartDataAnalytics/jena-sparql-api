package org.aksw.commons.io.seekable.impl;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.jena_sparql_api.io.deprecated.SeekableMatcher;

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
