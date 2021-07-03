package org.aksw.jena_sparql_api.io.deprecated;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;

import com.google.common.primitives.Bytes;

public class BoyerMooreMatcherFactory
	implements MatcherFactory 
{
	protected boolean isFwd;
	protected boolean lowerCaseMode;
	
	protected byte[] pat;
	
	/**
	 * This is the version of a character table that only tracks mismatch on the first instance of a char
	 * There is another approach which uses more memory using
	 * badCharacterTable[byteValueInText][positionOfMisMatch]
	 */
	protected int[] badCharacterTable;
	protected int[] goodSuffixTable;
	
	public BoyerMooreMatcherFactory(boolean isFwd, boolean lowerCaseMode, byte[] pat, int[] badCharacter, int[] goodSuffix) {
		super();
		this.isFwd = isFwd;
		this.lowerCaseMode = lowerCaseMode;
		this.pat = pat;
		this.badCharacterTable = badCharacter;
		this.goodSuffixTable = goodSuffix;
	}

	public static BoyerMooreMatcherFactory createFwd(byte[] pat) {
		return createFwd(pat, false);
	}

	public static BoyerMooreMatcherFactory createFwd(byte[] pat, boolean lowerCaseMode) {
		int[] badCharacterTable = BoyerMooreByteFromWikipedia.createBadCharacterTable(pat);
		int[] goodSuffixTable = BoyerMooreByteFromWikipedia.createGoodSuffixTable(pat);
		
		return new BoyerMooreMatcherFactory(true, lowerCaseMode, pat, badCharacterTable, goodSuffixTable);
	}	

	public static BoyerMooreMatcherFactory createBwd(byte[] pat) {
		return createBwd(pat, false);
	}

	public static BoyerMooreMatcherFactory createBwd(byte[] rawPat, boolean lowerCaseMode) {
		byte[] pat = rawPat.clone();
		Bytes.reverse(pat);
		
		int[] badCharacterTable = BoyerMooreByteFromWikipedia.createBadCharacterTable(pat);
		int[] goodSuffixTable = BoyerMooreByteFromWikipedia.createGoodSuffixTable(pat);
		
		return new BoyerMooreMatcherFactory(false, lowerCaseMode, pat, badCharacterTable, goodSuffixTable);
	}	

	@Override
	public SeekableMatcher newMatcher() {
		SeekableMatcher result = isFwd
				? !lowerCaseMode
					? new BoyerMooreMatcher(isFwd, pat, badCharacterTable, goodSuffixTable)
					: new BoyerMooreMatcher(isFwd, pat, badCharacterTable, goodSuffixTable) {
						protected byte getByte(Seekable seekable) throws IOException { return (byte)Character.toLowerCase(seekable.get()); } 
					  }
				: !lowerCaseMode
					? new BoyerMooreMatcher(isFwd, pat, badCharacterTable, goodSuffixTable) {
						protected boolean nextPos(Seekable seekable, int delta) throws IOException { return seekable.prevPos(delta); }
						protected boolean prevPos(Seekable seekable, int delta) throws IOException { return seekable.nextPos(delta); }
					}
					: new BoyerMooreMatcher(isFwd, pat, badCharacterTable, goodSuffixTable) {
						protected boolean nextPos(Seekable seekable, int delta) throws IOException { return seekable.prevPos(delta); }
						protected boolean prevPos(Seekable seekable, int delta) throws IOException { return seekable.nextPos(delta); }
						protected byte getByte(Seekable seekable) throws IOException { return (byte)Character.toLowerCase(seekable.get()); } 
					};
		return result;
	}

}
