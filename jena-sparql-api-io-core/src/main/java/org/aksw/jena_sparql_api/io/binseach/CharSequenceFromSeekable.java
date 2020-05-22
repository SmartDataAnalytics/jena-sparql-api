package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class CharSequenceFromSeekable
    implements CharSequence
{
    protected Seekable seekable;
    protected int offset;
    protected int end;
    protected int length;

    public CharSequenceFromSeekable(Seekable seekable) {
        this(seekable, 0, Integer.MAX_VALUE);
    }

    public CharSequenceFromSeekable(Seekable seekable, int offset, int end) {
        this.seekable = seekable;
        this.offset = offset;
        this.end = end;
        this.length = end - offset;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        try {
            int p = offset + index;
            seekable.nextPos(p);
            char result = (char)seekable.get();
            seekable.prevPos(p);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        throw new RuntimeException("not implemented exception");
//        Seekable clone = seekable.clone();
//        return new CharSequenceFromSeekable(clone, start, end);
    }
}
