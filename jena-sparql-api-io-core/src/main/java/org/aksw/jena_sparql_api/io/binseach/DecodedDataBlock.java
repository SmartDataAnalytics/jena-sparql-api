package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class DecodedDataBlock {
	protected long blockStart;
	protected long blockEnd;
	
	protected BufferSource bufferSource;
	
	public DecodedDataBlock nextBlock() throws IOException {
		return bufferSource.contentAfter(blockEnd);
	}

	public DecodedDataBlock prevBlock() throws IOException {
		return bufferSource.contentBefore(blockStart);
	}
	
	public long blockSize() {
		return data.length;
	}
	
	protected byte[] data;
	
	public DecodedDataBlock(
			BufferSource bufferSource,
			long blockStart,
			long blockEnd,
			byte[] data) {
		super();
		this.bufferSource = bufferSource;
		this.blockStart = blockStart;
		this.blockEnd = blockEnd;
		this.data = data;
	}

	public BufferSource getBufferSource() {
		return bufferSource;
	}
	
	public long getBlockStart() {
		return blockStart;
	}

	public long getBlockEnd() {
		return blockEnd;
	}

	public byte[] getData() {
		return data;
	}
}