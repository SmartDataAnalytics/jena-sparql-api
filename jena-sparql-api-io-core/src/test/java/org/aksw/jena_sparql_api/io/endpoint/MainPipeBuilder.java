package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.aksw.jena_sparql_api.io.filter.sys.FilterEngineFromSysFunction;
import org.aksw.jena_sparql_api.io.filter.sys.SysCallFn;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class MainPipeBuilder {
    public static Function<InputStream, InputStream> createCtConverter(Lang inLang, RDFFormat outFmt, String base) {
        return new Function<InputStream, InputStream>() {
            @Override
            public InputStream apply(InputStream in) {
                Model m = ModelFactory.createDefaultModel();
                RDFDataMgr.read(m, in, inLang);

                PipedOutputStream pout = new PipedOutputStream();
                PipedInputStream pin;
                try {
                    pin = new PipedInputStream(pout);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
                new Thread(() -> {
                    try(OutputStream xxx = pout) {
                        RDFDataMgr.write(xxx, m, outFmt);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                return pin;
            }

            @Override
            public String toString() { return "covertCt " + inLang + " -> " + outFmt; }
        };
    }

    public static void main(String[] main) throws IOException {

        // bzip.ifNeedsInputFile() -> well just check whether there is a file, otherwise create one...

        // FilterEngine identity = new FilterEngineJava(in -> in);

        // So the issue is if the successor requires a file
        // and now needs to know whether its predecessor creates one

        // Input types:
        // Static File (a well known type of byte source)
        // InputStream (stream of data from an anonymous source)
        // Active (Static) File (file being written to, but will be static once done)
        //   - passing the filename around is not safe before write is complete
        //   - if a file input is required, we best wait for it to complete
        //    Especially: as there is no file yet, do not try to create one via a concurrent stream
        //   - Active files may use a certain filename during writing, and afterwards
        //     be move them to a different location or be removed* alltogether
        //   - So once an active file is complete, we have no more guarantees that it still exists

        // * I think we can ignore the remove scenario: The callback facilities for files are intended
        // to allow for re-use of any file that needs to be generated anyway - so this means,
        // that the application logic has to ensure that file descriptors remain valid for a reasonable
        // amount of time

        SysCallFn encodeBzipCore = new SysCallFn() {
            @Override
            public String[] buildCheckCmd() { return new String[] {"/usr/bin/lbzip2", "--version"}; }
            @Override
            public String[] buildCmdForFileToStream(Path input) { return new String[] {"/usr/bin/lbzip2", "-czk", input.toString()}; };
            @Override
            public String toString() { return "encodeBzipCore"; }
        };

        SysCallFn decodeBzipCore = new SysCallFn() {
            @Override
            public String[] buildCheckCmd() { return new String[] {"/usr/bin/lbzip2", "--version"}; }
            @Override
            public String[] buildCmdForFileToStream(Path input) { return new String[] {"/usr/bin/lbzip2", "-cdk", input.toString()}; };
            @Override
            public String toString() { return "decodeBzipCore"; }
        };


        SysCallFn encodeGzipCore = new SysCallFn() {
            @Override
            public String[] buildCheckCmd() { return new String[] {"/bin/gzip", "--version"}; }
            @Override
            public String[] buildCmdForFileToStream(Path input) {
                return new String[] {"/bin/gzip", "-ck", input.toString()};
            };
            @Override
            public String toString() { return "encodeGzipCode"; }
        };

        FilterEngine decodeBzip = new FilterEngineFromSysFunction(decodeBzipCore);
        FilterEngine encodeGzip = new FilterEngineFromSysFunction(encodeGzipCore);


        FilterEngine ctConverter = new FilterEngineJava(
                createCtConverter(Lang.NTRIPLES, RDFFormat.TURTLE_PRETTY, "http://base/"));


        Destination source = Destinations.fromFile(Paths.get("/tmp/data.nt.bz2"));


        // Figure out how to add cancellation to the api...
        // Each destination may have at most 1 generating process (a static file is a destination without generating process)
        // So maybe it makes sense to cancel on destinations?

        // Create a destination for the turtle
        Destination destForTurtle = source
                .transferTo(decodeBzip)
                .pipeInto(ctConverter)
                .outputToFile(Paths.get("/tmp/converted.ttl"));


        // Compound operators


        // Apply gzip encoding of the content
//		destForTurtle
//			.transferTo(encodeGzip)
//				.outputToFile(Paths.get("/tmp/encoded.ttl.gz"))
//				.prepareStream()
//				.subscribe();

        // At the same time, read the turtle
        // ISSUE?? bzip gets started twice this way - because destForTurtle...outputToStream()
        // does not cache the result - actually this is fine: If we do not cache the result, we will
        // get a fresh stream - it seems reasonable for this to be intended behavior
        //
        //
        //
        //
        // What is needed to fix this? Do we need a subscription mechanism similar to rx?
        // Or is this out of scope?
        // The main scope is to have an abstraction for scala and system-call filters, so that
        // drop in replacement becomes possible
        //
        // Further, certain system calls require files as arguments, so streams may need to be serialized
        // to make it possible to pass them to the sys functions
        //
        // And finally, if a system call requires of a creation of a file anyway, then allow reuse of it
        // for further streaming
        //
        //
        // I'd like to avoid reinventing the wheel in that regard
        //

        try(InputStream in = destForTurtle.prepareStream().blockingGet()
            .execStream().blockingGet()) {
            Model m = ModelFactory.createDefaultModel();
            RDFDataMgr.read(m, in, Lang.TURTLE);
            System.out.println("#triples read " + m.size());
        }



        // TODO Restore example
        /**
         * Decode bzip into a file and simultaneously read from it
         *
         */
            /*
            try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                int i = 0;
                while((line = br.readLine()) != null) {
                    if(i % 10000 == 0) {
                        System.out.println("at line " + i);

                    }
                    ++i;
                    //System.out.println(line);
                }
            }*/
//		}

//		if(false) {
//		FilterEngine requiresFileSource = null;//new FilterEngineFromSysFunction(sysFunction);
//
//		Single<InputStreamSupplier> in = source
//				.transferTo(decodeBzip)
//					.ifNeedsFileInput(null, null)
//					.ifNeedsFileOutput(null, null)
//					.outputToFile(null)
//				//.outputToFile(null)
//				.transferTo(requiresFileSource) // should reuse the file output, registers for HotFile.whenReady()
//					.execStream();
//
//		source
//				.transferTo(decodeBzip)
//					.ifNeedsFileInput(null, null)
//					.ifNeedsFileOutput(null, null)
//				.pipeInto(decodeBzip)
//					.execStream();
//		}

//		bzip.getOutput().connectWith(identity.getInput())

//
//		FilterEngine identity = new FilterEngineJava(in -> in);
//
//		FilterEngine filter2 = new FilterEngineJava(in -> in);
//
//		FilterExecution exec = identity.forInput(() -> null).execStream();
//
//		filter2
//			.forInput(identity);


    }
}
