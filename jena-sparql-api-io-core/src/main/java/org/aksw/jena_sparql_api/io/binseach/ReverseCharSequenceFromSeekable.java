package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class ReverseCharSequenceFromSeekable implements CharSequence {
	protected Seekable seekable;
	protected int offset;
	protected int end;

	public ReverseCharSequenceFromSeekable(Seekable seekable) {
		this(seekable, 0, Integer.MAX_VALUE);
	}

	public ReverseCharSequenceFromSeekable(Seekable seekable, int offset, int end) {
		this.seekable = seekable;
		this.offset = offset;
		this.end = end;
	}

	@Override
	public int length() {
		return Integer.MAX_VALUE;
	}

	@Override
	public char charAt(int index) {
		try {
			int p = offset + index;
			seekable.prevPos(p);
			char result = (char) seekable.get();
			seekable.nextPos(p);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		// throw new RuntimeException("not implemented exception");
		Seekable clone = seekable.clone();
		return new CharSequenceFromSeekable(clone, start, end);

	}
}