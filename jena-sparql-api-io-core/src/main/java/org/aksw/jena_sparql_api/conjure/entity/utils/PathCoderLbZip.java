package org.aksw.jena_sparql_api.conjure.entity.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathCoderLbZip
	extends PathCoderSysBase
{
	public static void main(String[] args) throws Exception {
		
		PathCoderLbZip test = new PathCoderLbZip();
		//System.out.println("Cmd exists? " + test.cmdExists());
		
		test.encode(Paths.get("/tmp/test.txt"), Paths.get("/tmp/test.bz2"));
		test.decode(Paths.get("/tmp/test.bz2"), Paths.get("/tmp/hello.txt"));
	}

	@Override
	protected String[] buildCheckCmd() {
//		String[] result = {
//				"/bin/sh",
//				"-c",
//				"lbzip2 --version"
//			};
		String[] result = {"/usr/bin/lbzip2", "--version"};
		return result;
	}
//	
//	@Override
//	public Single<Integer> encode(Path input, Path output) {
////		String i = input.toString().replace("'", "\\'");
////		String o = output.toString().replace("'", "\\'");
//		
//		String[] cmd = { "/usr/bin/lbzip2", "-czk", input.toString() };
//
//		System.out.println(Arrays.asList(cmd));
//		
//		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
//		File outFile = output.toFile();
//		processBuilder.redirectOutput(outFile);
//		SimpleProcessExecutor x = SimpleProcessExecutor.wrap(processBuilder);
//		try {
//			return x.executeFuture().doOnDispose(() -> outFile.delete());
//		} catch (IOException | InterruptedException e) {
//			throw new RuntimeException();
//		}
//	}
//
//	@Override
//	public Single<Integer> decode(Path input, Path output) {
//		String[] cmd = { "/usr/bin/lbzip2", "-cdk", input.toString() };
//
//		System.out.println(Arrays.asList(cmd));
//		
//		ProcessBuilder processBuilder = new ProcessBuilder(cmd);
//		File outFile = output.toFile();
//		processBuilder.redirectOutput(outFile);
//		SimpleProcessExecutor x = SimpleProcessExecutor.wrap(processBuilder);
//		try {
//			return x.executeFuture().doOnDispose(() -> outFile.delete());
//		} catch (IOException | InterruptedException e) {
//			throw new RuntimeException();
//		}
//	}

	@Override
	protected String[] buildDecodeCmd(Path input) {
		String[] result = { "/usr/bin/lbzip2", "-cdk", input.toString() };
		return result;
	}

	@Override
	protected String[] buildEncodeCmd(Path input) {
		String[] result = { "/usr/bin/lbzip2", "-czk", input.toString() };
		return result;
	}
	
//	@Override
//	protected String[] buildDecodeCmd(String i, String o) {
//		// c = stdout, d = decompress, k = keep input
////		String[] result = {
////				"/bin/sh",
////				"-c",
////				"lbzip2 -cdk " + i + " > " + o
////			};
//		String[] result = {"/usr/bin/lbzip2", "-cdk", i, " > " + o};
//		return result;
//	}
//
//	//@Override
//	protected String[] buildEncodeCmd(String i, String o) {
//		// c = stdout, z = compress, k = keep input 
////		String[] result = {
////				"/bin/sh",
////				"-c",
////				"lbzip2 -czk " + i + " > " + o
////			};
//		String[] result = {"/usr/bin/lbzip2", "-czk", i, " > " + o};
//
//		return result;
//	}

}
