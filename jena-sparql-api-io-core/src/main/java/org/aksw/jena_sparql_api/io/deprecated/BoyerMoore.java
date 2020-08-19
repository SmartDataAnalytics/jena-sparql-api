package org.aksw.jena_sparql_api.io.deprecated;

import org.apache.jena.ext.com.google.common.primitives.Bytes;

/* Java program for Boyer Moore Algorithm with  
Good Suffix heuristic to find pattern in 
given text string */
/**
 * 
 * Based on https://www.geeksforgeeks.org/boyer-moore-algorithm-good-suffix-heuristic/
 * 
 * 
 * @author raven
 *
 */
class BoyerMoore {

	/**
	 * Preprocessing for strong good suffix rule 
	 * 
	 * @param shift
	 * @param bpos
	 * @param pattern
	 * @param m
	 */
	public static void preprocess_strong_suffix(int[] shift, int[] bpos, byte[] pattern) {
		int m = pattern.length;

		// m is the length of pattern
		int i = m, j = m + 1;
		bpos[i] = j;

		while (i > 0) {
			/*
			 * if character at position i-1 is not equivalent to character at j-1, then
			 * continue searching to right of the pattern for border
			 */
			while (j <= m && pattern[i - 1] != pattern[j - 1]) {
				/*
				 * the character preceding the occurrence of t in pattern P is different than
				 * the mismatching character in P, we stop skipping the occurrences and shift
				 * the pattern from i to j
				 */
				if (shift[j] == 0) {
					shift[j] = j - i;
				}

				// Update the position of next border
				j = bpos[j];
			}
			/*
			 * p[i-1] matched with p[j-1], border is found. store the beginning position of
			 * border
			 */
			i--;
			j--;
			bpos[i] = j;
		}
	}

	/**
	 * Preprocessing for case 2
	 *  
	 * @param shift
	 * @param bpos
	 * @param pat
	 * @param m
	 */
	public static void preprocess_case2(int[] shift, int[] bpos, byte[] pat) {
		int m = pat.length;

		int i, j;
		j = bpos[0];
		for (i = 0; i <= m; i++) {
			/*
			 * set the border position of the first character of the pattern to all indices
			 * in array shift having shift[i] = 0
			 */
			if (shift[i] == 0) {
				shift[i] = j;
			}

			/*
			 * suffix becomes shorter than bpos[0], use the position of next widest border
			 * as value of j
			 */
			if (i == j) {
				j = bpos[j];
			}
		}
	}

	/*
	 * Search for a pattern in given text using Boyer Moore algorithm with Good
	 * suffix rule
	 */
	static long searchForwardSimple(byte[] text, byte[] pat) {
		// s is shift of the pattern
		// with respect to text
		int s = 0, j;
		int m = pat.length;
		long n = text.length;

		int[] bpos = new int[m + 1];
		int[] shift = new int[m + 1];

		// initialize all occurrence of shift to 0
		for (int i = 0; i < m + 1; i++)
			shift[i] = 0;

		// do preprocessing
		preprocess_strong_suffix(shift, bpos, pat);
		preprocess_case2(shift, bpos, pat);

		long result = -1;
		while (s <= n - m) {
			j = m - 1;

			/*
			 * Keep reducing index j of pattern while characters of pattern and text are
			 * matching at this shift s
			 */
			while (j >= 0 && pat[j] == text[s + j]) {
				j--;
			}

			/*
			 * If the pattern is present at the current shift, then index j will become -1
			 * after the above loop
			 */
			if (j < 0) {
				result = s;
				break;
//				System.out.printf("pattern occurs at shift = %d\n", s);
//				s += shift[0];
			} else {

				/*
				 * pat[i] != pat[s+j] so shift the pattern shift[j+1] times
				 */
				s += shift[j + 1];
			}
		}
		
		return result;
	}
	
	static long searchBackwards(byte[] text, int startPos, byte[] fwdPat) {
		
		// TODO The even nicer solution might even avoid cloning the pattern
		byte[] pat = fwdPat.clone();
		Bytes.reverse(fwdPat);
		
		// s is shift of the pattern
		// with respect to text
		int s = startPos, j;
		int m = pat.length;
		//int n = text.length;

		int[] bpos = new int[m + 1];
		int[] shift = new int[m + 1];

		// initialize all occurrence of shift to 0
		for (int i = 0; i < m + 1; i++)
			shift[i] = 0;

		// do preprocessing
		preprocess_strong_suffix(shift, bpos, pat);
		preprocess_case2(shift, bpos, pat);

		long result = -1;
		while (s >= m) {
			j = m - 1;

			/*
			 * Keep reducing index j of pattern while characters of pattern and text are
			 * matching at this shift s
			 */
			while (j >= 0 && pat[j] == text[s - j]) {
				j--;
			}

			/*
			 * If the pattern is present at the current shift, then index j will become -1
			 * after the above loop
			 */
			if (j < 0) {
				result = s - (m - 1);
				break;
//				System.out.printf("pattern occurs at shift = %d\n", s);
//				s += shift[0];
			} else {

				/*
				 * pat[i] != pat[s+j] so shift the pattern shift[j+1] times
				 */
				int delta = shift[j + 1];
				s -= delta;
			}
		}
		
		return result;
	}

// Driver Code 
	public static void main(String[] args) throws Exception {
//		URI uri = new URI("file:///tmp/foobar.txt?test=true");
//		System.out.println(uri.toString().replaceAll("\\?.*", ""));

//		Path path = Paths.get(uri);
//		System.out.println(path);
		
		
		byte[] text = "ABAAAABAACDABA".getBytes();
		byte[] pat = "AAACD".getBytes();

		long pos = searchForwardSimple(text, pat);
//		byte[] pat = "ABA".getBytes();
		//long pos = searchBackwards(text, text.length, pat);
		System.out.println(pos);
	}
}

// This code is contributed by 29AjayKumar 
