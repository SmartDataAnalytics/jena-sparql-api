package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SeekableFromBufferSource
	implements Seekable
{
	// Index in the decoded data block
	protected int index;
	protected BufferSource bufferSource;
	protected DecodedDataBlock currentBlock;
	
	public SeekableFromBufferSource(int index, BufferSource bufferSource, DecodedDataBlock currentBlock) {
		super();
		this.index = index;
		this.bufferSource = bufferSource;
		this.currentBlock = currentBlock;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		int n = 0;
		if(currentBlock != null) {
			n = Math.min(dst.remaining(), currentBlock.data.length - index);
			dst.put(currentBlock.data, index, n);
			nextPos(n);
		}
		return n;
		//PageNavigator.readRemaining(dst, src)
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public long getPos() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void setPos(long pos) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void posToStart() throws IOException {
		currentBlock = bufferSource.contentAfter(-1);
		index = 0;
	}

	@Override
	public void posToEnd() throws IOException {
		currentBlock = bufferSource.contentBefore(bufferSource.size() + 1);
		index = currentBlock.getData().length - 1;;
	}

	@Override
	public byte get() throws IOException {
		byte result = currentBlock.getData()[index];
		return result;
	}

	@Override
	public boolean isPosBeforeStart() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isPosAfterEnd() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean nextPos(int len) throws IOException {
		int tgtIndex = index + len;
		DecodedDataBlock tmp = currentBlock;
		while(tgtIndex >= tmp.blockSize()) {
			tgtIndex -= tmp.blockSize();
			DecodedDataBlock next = tmp.nextBlock();
			if(next == null) {
				return false;
			}
			tmp = next;
		}
		index = tgtIndex;
		currentBlock = tmp;
		
		return true;
	}
	
	@Override
	public boolean prevPos(int len) throws IOException {
		int tgtIndex = index - len;
		DecodedDataBlock tmp = currentBlock;
		while(tgtIndex < 0) {
			DecodedDataBlock prev = tmp.nextBlock();
			tgtIndex += tmp.blockSize();
			if(prev == null) {
				return false;
			}
			tmp = prev;
		}
		index = tgtIndex;
		currentBlock = tmp;
		
		return true;
	}

}
