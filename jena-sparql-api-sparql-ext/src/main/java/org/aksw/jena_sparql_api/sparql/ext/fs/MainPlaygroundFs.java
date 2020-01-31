package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

public class MainPlaygroundFs {
	public static void main(String[] args) throws TikaException, IOException, SAXException, CompressorException {
		
		//Path path = Paths.get("/home/raven/.dcat/test3/downloads/gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl/_content/data.ttl");
//		Path path = Paths.get("/tmp/data.ttl.bz2");
		Path path = Paths.get("/tmp/data.hdt");

		System.out.println(
			ExprUtils.eval(
				ExprUtils.parse("<java:org.aksw.jena_sparql_api.sparql.ext.fs.probeContentType>('/tmp/data.ttl')"))
		);
		System.out.println(
				ExprUtils.eval(
					ExprUtils.parse("<java:org.aksw.jena_sparql_api.sparql.ext.fs.probeEncoding>('/tmp/data.ttl.bz2')"))
		);
		System.out.println(
				ExprUtils.eval(
					ExprUtils.parse("<http://jsa.aksw.org/fn/fs/probeContentType>('/tmp/data.ttl')"))
		);
		System.out.println(
				ExprUtils.eval(
					ExprUtils.parse("<http://jsa.aksw.org/fn/fs/probeEncoding>('/tmp/data.ttl.bz2')"))
		);
		
		
//		String ct = Files.probeContentType(path);
//
//		//try(InputStream in = Files.newInputStream(path)) {
//			//Metadata metadata = new Metadata();
//			String metadata = CompressorStreamFactory.detect(TikaInputStream.get(path));
//			//cp.parse(in, null, metadata, new ParseContext());
//			System.out.println(metadata);
//		//}
//		System.out.println(ct);
		
//		new TikaFile
//		TikaConfig tika = new TikaConfig();
//
//		List<File> myListOfFiles = Arrays.asList(new File("/home/raven/.dcat/test3/downloads/gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl/_content/data.ttl"));
//		for (File f : myListOfFiles) {
//		   Metadata metadata = new Metadata();
//		   metadata.set(Metadata.RESOURCE_NAME_KEY, f.toString());
//		   MediaType mimetype = tika.getDetector()
//				   .detect(TikaInputStream.get(f), metadata);
//		   System.out.println("File " + f + " is " + mimetype);
//		}
////		for (InputStream is : myListOfStreams) {
////			MediaType mimetype = tika.getDetector().detect(
////					new FileInputStream(f), new Metadata());
////		   System.out.println("Stream " + is + " is " + mimetype);
////		}	}
	}
}
