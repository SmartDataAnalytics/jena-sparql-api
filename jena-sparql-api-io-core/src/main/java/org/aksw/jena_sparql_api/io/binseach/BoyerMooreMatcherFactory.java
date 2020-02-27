package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

import com.google.common.primitives.Bytes;

public class BoyerMooreMatcherFactory
	implements MatcherFactory 
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
	
	public BoyerMooreMatcherFactory(boolean isFwd, byte[] pat, int[] badCharacter, int[] goodSuffix) {
		super();
		this.isFwd = isFwd;
		this.pat = pat;
		this.badCharacterTable = badCharacter;
		this.goodSuffixTable = goodSuffix;
	}
	
	public static BoyerMooreMatcherFactory createFwd(byte[] pat) {
		int[] badCharacterTable = BoyerMooreByteFromWikipedia.createBadCharacterTable(pat);
		int[] goodSuffixTable = BoyerMooreByteFromWikipedia.createGoodSuffixTable(pat);
		
		return new BoyerMooreMatcherFactory(true, pat, badCharacterTable, goodSuffixTable);
	}	

	public static BoyerMooreMatcherFactory createBwd(byte[] rawPat) {
		byte[] pat = rawPat.clone();
		Bytes.reverse(pat);
		
		int[] badCharacterTable = BoyerMooreByteFromWikipedia.createBadCharacterTable(pat);
		int[] goodSuffixTable = BoyerMooreByteFromWikipedia.createGoodSuffixTable(pat);
		
		return new BoyerMooreMatcherFactory(false, pat, badCharacterTable, goodSuffixTable);
	}	

	@Override
	public SeekableMatcher newMatcher() {
		SeekableMatcher result = isFwd
				? new BoyerMooreMatcher(isFwd, pat, badCharacterTable, goodSuffixTable)
				: new BoyerMooreMatcher(isFwd, pat, badCharacterTable, goodSuffixTable) {
					protected boolean nextPos(Seekable seekable, int delta) throws IOException { return seekable.prevPos(delta); }
					protected boolean prevPos(Seekable seekable, int delta) throws IOException { return seekable.nextPos(delta); }
				  };
		return result;
	}

}
