package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.nio.file.Paths;

import org.aksw.jena_sparql_api.io.filter.sys.SysCallFn;

import io.reactivex.Single;

public class MainPipeBuilder {
	public static void main(String[] main) throws IOException {

		// bzip.ifNeedsInputFile() -> well just check whether there is a file, otherwise create one...
		
		FilterEngine identity = new FilterEngineJava(in -> in);
		
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
		
		SysCallFn sysFunction = null;//new SysCallFnLbZipEncode();
		FilterEngine bzip = null;//new FilterEngineFromSysFunction(sysFunction);

		FilterEngine requiresFileSource = null;//new FilterEngineFromSysFunction(sysFunction);

		Destination source = new DestinationFromFile(Paths.get("/tmp/data.nt"));
		
		Single<InputStreamSupplier> in = source
				.transferTo(bzip)
					.ifNeedsFileInput(null, null)
					.ifNeedsFileOutput(null, null)
					.outputToFile(null)
				//.outputToFile(null)
				.transferTo(requiresFileSource) // should reuse the file output, registers for HotFile.whenReady()
					.execStream();

		source
				.transferTo(bzip)
					.ifNeedsFileInput(null, null)
					.ifNeedsFileOutput(null, null)
				.pipeInto(bzip)
					.execStream();

		
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
