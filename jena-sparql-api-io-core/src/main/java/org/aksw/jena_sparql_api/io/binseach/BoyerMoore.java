package org.aksw.jena_sparql_api.io.binseach;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	static void preprocess_strong_suffix(int[] shift, int[] bpos, char[] pattern, int m) {
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
				if (shift[j] == 0)
					shift[j] = j - i;

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
	static void preprocess_case2(int[] shift, int[] bpos, char[] pat, int m) {
		int i, j;
		j = bpos[0];
		for (i = 0; i <= m; i++) {
			/*
			 * set the border position of the first character of the pattern to all indices
			 * in array shift having shift[i] = 0
			 */
			if (shift[i] == 0)
				shift[i] = j;

			/*
			 * suffix becomes shorter than bpos[0], use the position of next widest border
			 * as value of j
			 */
			if (i == j)
				j = bpos[j];
		}
	}

	/*
	 * Search for a pattern in given text using Boyer Moore algorithm with Good
	 * suffix rule
	 */
	static void search(char[] text, char[] pat) {
		// s is shift of the pattern
		// with respect to text
		int s = 0, j;
		int m = pat.length;
		int n = text.length;

		int[] bpos = new int[m + 1];
		int[] shift = new int[m + 1];

		// initialize all occurrence of shift to 0
		for (int i = 0; i < m + 1; i++)
			shift[i] = 0;

		// do preprocessing
		preprocess_strong_suffix(shift, bpos, pat, m);
		preprocess_case2(shift, bpos, pat, m);

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
				System.out.printf("pattern occurs at shift = %d\n", s);
				s += shift[0];
			} else {

				/*
				 * pat[i] != pat[s+j] so shift the pattern shift[j+1] times
				 */
				s += shift[j + 1];
			}
		}

	}

// Driver Code 
	public static void main(String[] args) throws Exception {
		URI uri = new URI("file:///tmp/foobar.txt?test=true");
		System.out.println(uri.toString().replaceAll("\\?.*", ""));

//		Path path = Paths.get(uri);
//		System.out.println(path);
		
		
		char[] text = "ABAAAABAACD".toCharArray();
		char[] pat = "ABA".toCharArray();
		search(text, pat);
	}
}

// This code is contributed by 29AjayKumar 
