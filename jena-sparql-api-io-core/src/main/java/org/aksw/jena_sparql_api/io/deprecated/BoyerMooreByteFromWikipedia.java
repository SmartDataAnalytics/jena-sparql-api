package org.aksw.jena_sparql_api.io.deprecated;

import org.apache.jena.ext.com.google.common.primitives.Bytes;

public class BoyerMooreByteFromWikipedia {
    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. If it is not a substring, return -1.
     *
     * There is no Galil because it only generates one match.
     *
     * @param haystack The string to be scanned
     * @param needle The target string to search
     * @return The start index of the substring
     */
    public static int indexOf(byte[] haystack, byte[] needle) {
        if (needle.length == 0) {
            return 0;
        }
        int charTable[] = createBadCharacterTable(needle);
        int offsetTable[] = createGoodSuffixTable(needle);
        for (int i = needle.length - 1, j; i < haystack.length;) {
            for (j = needle.length - 1; needle[j] == haystack[i]; --i, --j) {
                if (j == 0) {
                    return i;
                }
            }
            // i += needle.length - j; // For naive method
            i += Math.max(offsetTable[needle.length - 1 - j], charTable[haystack[i]]);
        }
        return -1;
    }

    public static int[] createBadCharacterTable(byte[] pattern) {
        final int ALPHABET_SIZE = 256;
        int m = pattern.length;
        int[] table = new int[ALPHABET_SIZE];

        for (int i = 0; i < ALPHABET_SIZE; i++) {
            //table[i] = m;
        	table[i] = -1;
        }
        for (int i = 0; i < m - 1; i++) {
        	int value = pattern[i] & 0xff; // & 0xff -> get unsigned int value of the byte
//        	int shift = m - 1 - i;
//            table[value] = shift;
        	table[value] = i;
        }
        return table;
    }

    /**
     * Makes the jump table based on the mismatched character information.
     * (bad character rule?).
     */
    public static int[] makeByteTableOld(byte[] needle) {
        final int ALPHABET_SIZE = 256;
        int[] table = new int[ALPHABET_SIZE];
        for (int i = 0; i < table.length; ++i) {
            table[i] = needle.length;
        }
        for (int i = 0; i < needle.length - 2; ++i) {
        	int value = needle[i] & 0xff; // & 0xff -> get unsigned int value of the byte
            table[value] = needle.length - 1 - i;
        }
        return table;
    }

    
    /**
     * Makes the jump table based on the scan offset which mismatch occurs.
     * (bad character rule).
     */
//    public static int[] createGoodSuffixTable(byte[] pattern) {
//    	int m = pattern.length;
//        int[] table = new int[m];
//        int lastPrefixPosition = m;
//        for (int i = m; i > 0; --i) {
//            if (isPrefix(pattern, i)) {
//                lastPrefixPosition = i;
//            }
//            table[m - i] = lastPrefixPosition - i + m;
//        }
//        for (int i = 0; i < m - 1; ++i) {
//            int slen = suffixLength(pattern, i);
//            table[m - 1 - slen] = m - 1 - i + slen;
//        }
//        return table;
//    }

    public static int[] createGoodSuffixTable(byte[] pattern) {
    	int m = pattern.length;
        int[] f = new int[m + 1];
        int[] s = new int[m + 1];
        
        int i = m;
        int j = m + 1;
        f[i] = j;
        //int i = m;
        //for(int i = m - 1; i >= 0; --i) {
        while(i > 0) {
        	// https://www.inf.hs-flensburg.de/lang/algorithmen/pattern/bmen.htm
        	while(j <= m && pattern[i - 1] != pattern[j - 1]) {
        		if(s[j] == 0) {
        			s[j] = j - i;
        		}
    			j = f[j];
        	}
        	--i;
        	--j;
        	f[i] = j;
        	
//            if (isPrefix(pattern, i)) {
//                lastPrefixPosition = i;
//            }
            //table[i] = lastPrefixPosition;
            //table[m - i] = j - i + m;
        }
        
        j = f[0];
        for(i = 0; i <= m; ++i) {
        	if(s[i] == 0) {
        		s[i] = j;
        	}
        	
        	if(i == j) {
        		j = f[j];
        	}
        }
//        for (int i = 0; i < m - 1; ++i) {
//            int slen = suffixLength(pattern, i);
//            table[m - 1 - slen] = m - 1 - i + slen;
//        }
        return s;
    }
    
    /**
     * Makes the jump table based on the scan offset which mismatch occurs.
     * This is only different from the java version that the array is reversed
     * 
     * (good suffix)
     */
    public static int[] makeOffsetTableFromC(byte[] needle) { // this is delta2 in the c code
    	int m = needle.length;

        int[] table = new int[m];
        int lastPrefixPosition = m;
        for (int i = m - 1; i >= 0; --i) {
            if (isPrefix(needle, i + 1)) {
                lastPrefixPosition = i + 1;
            }
            table[i] = lastPrefixPosition + (m - 1 - i);
        }
        for (int i = 0; i < m - 1; ++i) {
            int slen = suffixLength(needle, i);
            if(needle[i - slen] != needle[m - 1 - slen]) {
            	table[m - 1 - slen] = m - 1 - i + slen;
            }
            //table[slen] = m - 1 - i + slen;
        }
//      int last_prefix_index = patlen-1;
        //
//             // first loop
//             for (p=patlen-1; p>=0; p--) {
//                 if (isPrefix(pat, patlen, p+1)) {
//                     last_prefix_index = p+1;
//                 }
//                 delta2[p] = lastPrefix_index + (patlen-1 - p);
//             }
        //
//             // second loop
//             for (p=0; p < patlen-1; p++) {
//                 int slen = suffixLength(pat, patlen, p);
//                 if (pat[p - slen] != pat[patlen-1 - slen]) {
//                     delta2[patlen-1 - slen] = patlen-1 - p + slen;
//                 }
//             }
        // }
        return table;
    }

    /**
     * Is needle[p:end] a prefix of needle?
     */
    public static boolean isPrefix(byte[] pattern, int p) {
    	int m = pattern.length;
        for (int i = p, j = 0; i < m; ++i, ++j) {
            if (pattern[i] != pattern[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the maximum length of the substring ends at p and is a suffix.
     * (good suffix rule)
     */
    public static int suffixLength(byte[] needle, int p) {
        int len = 0;
        for (int i = p, j = needle.length - 1;
                 i >= 0 && needle[i] == needle[j]; --i, --j) {
            len += 1;
        }
        return len;
    }
    
    
 // GOOD SUFFIX RULE.
 // delta2 table: given a mismatch at pat[pos], we want to align
 // with the next possible full match could be based on what we
 // know about pat[pos+1] to pat[patlen-1].
 //
 // In case 1:
 // pat[pos+1] to pat[patlen-1] does not occur elsewhere in pat,
 // the next plausible match starts at or after the mismatch.
 // If, within the substring pat[pos+1 .. patlen-1], lies a prefix
 // of pat, the next plausible match is here (if there are multiple
 // prefixes in the substring, pick the longest). Otherwise, the
 // next plausible match starts past the character aligned with
 // pat[patlen-1].
 //
 // In case 2:
 // pat[pos+1] to pat[patlen-1] does occur elsewhere in pat. The
 // mismatch tells us that we are not looking at the end of a match.
 // We may, however, be looking at the middle of a match.
 //
 // The first loop, which takes care of case 1, is analogous to
 // the KMP table, adapted for a 'backwards' scan order with the
 // additional restriction that the substrings it considers as
 // potential prefixes are all suffixes. In the worst case scenario
 // pat consists of the same letter repeated, so every suffix is
 // a prefix. This loop alone is not sufficient, however:
 // Suppose that pat is "ABYXCDBYX", and text is ".....ABYXCDEYX".
 // We will match X, Y, and find B != E. There is no prefix of pat
 // in the suffix "YX", so the first loop tells us to skip forward
 // by 9 characters.
 // Although superficially similar to the KMP table, the KMP table
 // relies on information about the beginning of the partial match
 // that the BM algorithm does not have.
 //
 // The second loop addresses case 2. Since suffix_length may not be
 // unique, we want to take the minimum value, which will tell us
 // how far away the closest potential match is.
// byte[] make_delta2(byte[] pat) {
//	 int patlen = pat.length;
//	 byte[] delta2 = new byte[patlen];
//     int p;
//     int last_prefix_index = patlen-1;
//
//     // first loop
//     for (p=patlen-1; p>=0; p--) {
//         if (isPrefix(pat, patlen, p+1)) {
//             last_prefix_index = p+1;
//         }
//         delta2[p] = lastPrefix_index + (patlen-1 - p);
//     }
//
//     // second loop
//     for (p=0; p < patlen-1; p++) {
//         int slen = suffixLength(pat, patlen, p);
//         if (pat[p - slen] != pat[patlen-1 - slen]) {
//             delta2[patlen-1 - slen] = patlen-1 - p + slen;
//         }
//     }
// }

}
