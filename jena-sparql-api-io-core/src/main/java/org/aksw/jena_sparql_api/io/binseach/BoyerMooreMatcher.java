package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

public class BoyerMooreMatcher {	
	protected int[] bpos;
	protected int[] shift;

	public BoyerMooreMatcher(int[] bpos, int[] shift) {
		super();
		this.bpos = bpos;
		this.shift = shift;
	}
	
	public static BoyerMooreMatcher create(byte[] pat) {
		// s is shift of the pattern
		// with respect to text
		int s = 0, j;
		int m = pat.length;
		//long n = text.length;

		int[] bpos = new int[m + 1];
		int[] shift = new int[m + 1];

		// initialize all occurrence of shift to 0
		for (int i = 0; i < m + 1; i++)
			shift[i] = 0;

		// do preprocessing
		BoyerMoore.preprocess_strong_suffix(shift, bpos, pat, m);
		BoyerMoore.preprocess_case2(shift, bpos, pat, m);
		
		return new BoyerMooreMatcher(bpos, shift);
	}
	
//	public static searchBackwards(ByteBufferSupplier pager, long pos) {
//		long p = pos;
//        outer: while(p > 0) {
//    		ByteBuffer buf = pager.get(p).duplicate();
//            int index = buffer.position();
//
//	        for(i = index; i >= 0; --i) {
//	            byte c = buffer.get(i);
//	            if(c == delimiter) {
//	            	break outer;
//	            }
//	        }
//	        
//	        index = pageSize - 1;
//        }
//        
//        p = Math.max(0, p);
//        //i = Math.max(0, i);
//        
//        long result = p * pageSize + (long)i;
//        return result;
//		
//	}
}
