package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class BoyerMooreMatcher {
	protected byte[] pat;
	protected int[] bpos;
	protected int[] shift;
	protected int m; // pattern length

	public BoyerMooreMatcher(byte[] pat, int[] bpos, int[] shift, int m) {
		super();
		this.pat = pat;
		this.bpos = bpos;
		this.shift = shift;
		this.m = m;
	}
	
	public static BoyerMooreMatcher create(byte[] pat) {
		// s is shift of the pattern
		// with respect to text
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
		
		return new BoyerMooreMatcher(pat, bpos, shift, m);
	}
	
	
	public void searchFwd(PageNavigator pn) throws IOException {		
		while(pn.canNextPos()) {
			int j = m - 1;

			//pn.setPos(s + j);
//			boolean couldMove = pn.nextPos(j);
//			if(!couldMove) {
//				break;
//			}
	
			/*
			 * Keep reducing index j of pattern while characters of pattern and text are
			 * matching at this shift s
			 */
			while(j > 0 && pat[j] == pn.get()) {
				--j;
				pn.prevPos();
			}
			// If the final pattern character equals the one in the buffer, we have a match
			boolean isMatch = j == 0 && pat[j] == pn.get();

//			while (j >= 0 && pat[j] == pn.get()) {
//				j--;
//			}

			/*
			 * If the pattern is present at the current shift, then index j will become -1
			 * after the above loop
			 */
			//if (j < 0) {
			if(isMatch) {
				// pn.nextPos();
				break;
//				System.out.printf("pattern occurs at shift = %d\n", s);
//				s += shift[0];
			} else {

				/*
				 * pat[i] != pat[s+j] so shift the pattern shift[j+1] times
				 */
				int sh = shift[j + 1];

				// Move back to the attempted matching position of s
				int delta = m - 1 - j;
				delta += sh;
				//s += shift[j + 1];
				pn.nextPos(delta);
			}
		}
		
		//return result;
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
