package org.aksw.jena_sparql_api.conjure.entity.utils;

import java.nio.file.Path;
import java.nio.file.Paths;


public class PathCoderGzip
    extends PathCoderSysBase
{
    public static void main(String[] args) throws Exception {

        PathCoderGzip test = new PathCoderGzip();
        System.out.println("Cmd exists? " + test.cmdExists());

        test.encode(Paths.get("/tmp/test.txt"), Paths.get("/tmp/test.bz2"));
        test.decode(Paths.get("/tmp/test.bz2"), Paths.get("/tmp/hello.txt"));
    }

    @Override
    protected String[] buildCheckCmd() {
//		String[] result = {
//				"/bin/sh",
//				"-c",
//				"gzip --version"
//			};

        String[] result = {"/bin/gzip", "--version"};
        return result;
    }

    @Override
    protected String[] buildDecodeCmd(Path input) {
        // c = stdout, d = decompress, k = keep input
//		String[] result = {
//				"/bin/sh",
//				"-c",
//				"gzip -cdk " + i + " > " + o
//			};
        String[] result = {"/bin/gzip", "-cdk", input.toString()};

        return result;
    }

    //@Override
    protected String[] buildEncodeCmd(Path input) {
        // c = stdout, k = keep input
//		String[] result = {
//				"/bin/sh",
//				"-c",
//				"gzip -ck " + i + " > " + o
//			};
        String[] result = {"/bin/gzip", "-ck", input.toString()};
        return result;
    }

}
