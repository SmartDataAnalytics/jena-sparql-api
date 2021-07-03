package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;

public class ReverseCharSequenceFromSeekable
  implements CharSequence
{
    protected Seekable seekable;
    protected int offset;
    protected int end;
    protected int length;

    public ReverseCharSequenceFromSeekable(Seekable seekable) {
        this(seekable, 0, Integer.MAX_VALUE);
    }

    public ReverseCharSequenceFromSeekable(Seekable seekable, int offset, int end) {
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
//        if(index == 16777673) {
//            System.err.println("DEBUG POiNT");
//        }
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
         throw new RuntimeException("not implemented exception");
//        Seekable clone = seekable.clone();
//        return new ReverseCharSequenceFromSeekable(clone, start, end);

    }
}