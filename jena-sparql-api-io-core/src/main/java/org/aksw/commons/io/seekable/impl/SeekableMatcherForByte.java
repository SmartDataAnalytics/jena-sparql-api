package org.aksw.commons.io.seekable.impl;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.jena_sparql_api.io.deprecated.SeekableMatcher;

public class SeekableMatcherForByte 
	implements SeekableMatcher
{
	/**
	 * The byte which to match
	 */
	protected byte delimiter;

	protected boolean isFwd;
	protected boolean toLower;


	public SeekableMatcherForByte(byte delimiter, boolean isFwd, boolean toLower) {
		super();
		this.delimiter = delimiter;
		this.isFwd = isFwd;
		this.toLower = toLower;
	}

	@Override
	public boolean isForward() {
		return isFwd;
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean find(Seekable seekable) throws IOException {
		boolean posChanged = seekable.posToNext(delimiter);
		if(posChanged) {
			
		}
		return false;
	}

}
