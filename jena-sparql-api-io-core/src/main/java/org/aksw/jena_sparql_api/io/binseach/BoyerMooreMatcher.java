package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

public class BoyerMooreMatcher {
	protected byte[] pat;
	
	/**
	 * This is the version of a character table that only tracks mismatch on the first instance of a char
	 * There is another approach which uses more memory using
	 * badCharacterTable[byteValueInText][positionOfMisMatch]
	 */
	protected int[] badCharacterTable;
	protected int[] goodSuffixTable;

	protected boolean returningFromMatch = false;
	
	public BoyerMooreMatcher(byte[] pat, int[] badCharacter, int[] goodSuffix) {
		super();
		this.pat = pat;
		this.badCharacterTable = badCharacter;
		this.goodSuffixTable = goodSuffix;
	}
	
	public static BoyerMooreMatcher create(byte[] pat) {
		// s is shift of the pattern
		// with respect to text
		//long n = text.length;
		int[] byteTable = BoyerMooreByteFromWikipedia.createBadCharacterTable(pat);
		
//		int[] argh = BoyerMooreByteFromWikipedia.makeOffsetTableFromC(pat);
		int[] delta2 = BoyerMooreByteFromWikipedia.createGoodSuffixTable(pat);
		
		System.out.println(Arrays.toString(byteTable));
//		System.out.println(Arrays.toString(argh));
		System.out.println(Arrays.toString(delta2));
		return new BoyerMooreMatcher(pat, byteTable, delta2);
		
		
//		int[] bpos = new int[m + 1];
//		int[] shift = new int[m + 1];
//
//		// initialize all occurrence of shift to 0
//		for (int i = 0; i < m + 1; i++) {
//			shift[i] = 0;
//		}
//
//		// do preprocessing
//		BoyerMoore.preprocess_strong_suffix(shift, bpos, pat);
//		BoyerMoore.preprocess_case2(shift, bpos, pat);
//		
//		return new BoyerMooreMatcher(pat, bpos, shift);
	}
	
	
	public boolean searchFwd(PageNavigator pn) throws IOException {
		
		int m = pat.length;

		int start = 0;
		if(returningFromMatch) {
//			System.out.println("Returning at: " + pn.getPos());
//			int delta = pat.length;//shift[0];
//			if(!pn.nextPos(delta)) {
//				return false;
//			}
////			System.out.println("Now at: " + pn.getPos());
			start += m;
		}
		start += m - 1;
		pn.nextPos(start);
		

		while(true) {
			int j = m - 1;

//			// Start maching from the end of the pattern
//			// If we cannot advance to that pos, we are done
//			if(!pn.nextPos(j)) {
//				break;
//			}

			//pn.setPos(s + j);
//			boolean couldMove = pn.nextPos(j);
//			if(!couldMove) {
//				break;
//			}
	
			/*
			 * Keep reducing index j of pattern while characters of pattern and text are
			 * matching at this shift s
			 */
//			System.out.println("pos: " + pn.getPos());
			byte byteAtPos = 0;
			while(j > 0 && pat[j] == (byteAtPos = pn.get())) {
				--j;
				pn.prevPos();
				// NOTE prevPos is assumed to always change position
//				if(!pn.prevPos()) {
//					throw new RuntimeException("should not happen");
//				}
			}
			// If the final pattern character equals the one in the buffer, we have a match
			boolean isMatch = j == 0 && pat[j] == (byteAtPos = pn.get());

//			while (j >= 0 && pat[j] == pn.get()) {
//				j--;
//			}

			/*
			 * If the pattern is present at the current shift, then index j will become -1
			 * after the above loop
			 */
			//if (j < 0) {
			if(isMatch) {
//				System.out.println("Match at: " + pn.getPos());
				returningFromMatch = true;
				// pn.nextPos();
				return true;
//				System.out.printf("pattern occurs at shift = %d\n", s);
//				s += shift[0];
			} else {

				// The bytes we have to go back in order to reach the position
				// from where we started matching 
				int backtrack = m - 1 - j;
				int rawCharShift = badCharacterTable[byteAtPos & 0xff];
				int charShift = backtrack + rawCharShift;
				int suffixShift = goodSuffixTable[j];
				
				int effectiveShift = charShift > suffixShift
						? charShift
						: suffixShift;
				
				if(!pn.nextPos(effectiveShift)) {
					break;
				}
				
//				long after = pn.getPos();
//				if(after - before != shift) {
//					System.out.println("Fail");
//				}
			}
		}
		
		return false;
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
