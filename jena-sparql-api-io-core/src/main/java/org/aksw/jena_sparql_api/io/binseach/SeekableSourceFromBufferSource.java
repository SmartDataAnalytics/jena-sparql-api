package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class SeekableSourceFromBufferSource
	implements SeekableSource
{
	protected BufferSource bufferSource;
	
	public SeekableSourceFromBufferSource(BufferSource bufferSource) {
		super();
		this.bufferSource = bufferSource;
	}

	@Override
	public boolean supportsAbsolutePosition() {
		return true;
	}

	@Override
	public Seekable get(long pos) throws IOException {
		DecodedDataBlock block = bufferSource.contentBefore(pos);
		Seekable result = null;
		if(block != null) {
			result = new SeekableFromBufferSource(0, bufferSource, block);
		}
		
		return result;
	}

	@Override
	public long size() throws IOException {
		long result = bufferSource.size();
		return result;
	}
	

}
