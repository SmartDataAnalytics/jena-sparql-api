package fr.inrialpes.tyrexmo.testqc;

public class NanoMicroBenchmark {
	public static void main(String[] args) {
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
