package fr.inrialpes.tyrexmo.testqc;

public class NanoMicroBenchmark {

	public static void main(String[] args) {
		test(100000000l, 0, 30);
	}

	public static void test(long max, int depth, int maxDepth) {
		if(depth >= maxDepth) {
			System.out.println("Done");
			return;
		}

		int r = 0;
		long start = System.nanoTime();

		for(int i = 0; i < max; ++i) {
			r += i;
		}

		long end = System.nanoTime();

		long elapsed = end - start;
		System.out.println("Elpased time for " + max + " iterations: " + elapsed + " computed: " + r);

		if(elapsed == 0) {
			test(max + (max >> 1), depth + 1, maxDepth);
		} else {
			test(max >> 1, depth + 1, maxDepth);
		}
	}

	public static void mainX(String[] args) {
		long w = 0;
		for(int i = 0; i < 100000; ++i) {
			w += i;
		}
		System.out.println("Done warmup: " + w);

		for(int i = 0; i < 10; ++ i) {
			long n = (long)Math.pow(10, i);

			long begin = System.nanoTime();

			int v = 0;
			for(int j = 0; j < n; ++j) {
				v = v + j;
			}

			long end = System.nanoTime();
			long elapsed = end - begin;
			double s = elapsed / (double)1000000000l;
			System.out.println("Time: " + i + "(" + n + "): " + elapsed + " in sec: " + s + " --- " + v);
		}



	}
}
