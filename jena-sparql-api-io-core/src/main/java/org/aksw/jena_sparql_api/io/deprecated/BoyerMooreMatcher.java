package org.aksw.jena_sparql_api.io.deprecated;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;

public class BoyerMooreMatcher
	implements SeekableMatcher
{
	protected boolean isFwd;
	protected byte[] pat;
	
	/**
	 * This is the version of a character table that only tracks mismatch on the first instance of a char
	 * There is another approach which uses more memory using
	 * badCharacterTable[byteValueInText][positionOfMisMatch]
	 */
	protected int[] badCharacterTable;
	protected int[] goodSuffixTable;

	protected boolean returningFromMatch = false;
	
	
	
	public BoyerMooreMatcher(boolean isFwd, byte[] pat, int[] badCharacter, int[] goodSuffix) {
		super();
		this.isFwd = isFwd;
		this.pat = pat;
		this.badCharacterTable = badCharacter;
		this.goodSuffixTable = goodSuffix;
	}
	

	@Override
	public void resetState() {
		this.returningFromMatch = false;
	}
	
	@Override
	public boolean isForward() {
		return true;
	}

	protected boolean nextPos(Seekable seekable, int delta) throws IOException {
		return seekable.nextPos(delta);
	}

	protected boolean prevPos(Seekable seekable, int delta) throws IOException {
		return seekable.prevPos(delta);
	}
	
	protected byte getByte(Seekable seekable) throws IOException {
		return seekable.get();
	}
	
	@Override
	public boolean find(Seekable pn) throws IOException {
		
		int m = pat.length;

		int start = 0;
		if(returningFromMatch) {
			start += goodSuffixTable[0];
		}
		start += m - 1;
		// Bail out if there is insufficient data available
		if(!nextPos(pn, start)) {
			return false;
		}
		

		while(true) {
			int j = m - 1;

			/*
			 * Keep reducing index j of pattern while characters of pattern and text are
			 * matching at this shift s
			 */
//			System.out.println("pos: " + pn.getPos());
			byte byteAtPos = 0;
			while(j > 0 && pat[j] == (byteAtPos = getByte(pn))) {
				--j;
				prevPos(pn, 1);
				// NOTE prevPos is assumed to always change position
//				if(!pn.prevPos()) {
//					throw new RuntimeException("should not happen");
//				}
			}
			// If the final pattern character equals the one in the buffer, we have a match
			boolean isMatch = j == 0 && pat[j] == (byteAtPos = getByte(pn));

			/*
			 * If the pattern is present at the current shift, then index j will become -1
			 * after the above loop
			 */
			//if (j < 0) {
			if(isMatch) {
//				System.out.println("Match at: " + pn.getPos());
				returningFromMatch = true;
				return true;
			} else {

				// The bytes we have to go back in order to reach the position
				// from where we started matching 
				int backtrack = m - 1 - j;

				int charShiftEntry = badCharacterTable[byteAtPos & 0xff];
				
				int charShift = backtrack + j - charShiftEntry;
				int suffixShift = goodSuffixTable[j + 1];

				int bestShift = charShift > suffixShift
						? charShift
						: suffixShift;
				
//				if(effectiveShift == suffixShift && effectiveShift != charShift) {
//					System.out.println("Used suffix shift");
//				}
//				effectiveShift = charShift;
				int effectiveShift = bestShift + backtrack;
				
				
				//effectiveShift += backtrack;
				if(!nextPos(pn, effectiveShift)) {
					break;
				}				
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
