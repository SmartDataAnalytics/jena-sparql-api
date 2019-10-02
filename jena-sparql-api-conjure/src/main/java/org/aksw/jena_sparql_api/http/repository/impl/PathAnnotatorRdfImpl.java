package org.aksw.jena_sparql_api.http.repository.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.aksw.jena_sparql_api.http.repository.api.PathAnnotatorRdf;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.ResourceUtils;

public class PathAnnotatorRdfImpl
	implements PathAnnotatorRdf
{
	
	protected String suffix = ".meta";
	
	public Resource getRecord(Path path) {
		
		Resource result;
		
		if(Files.exists(path)) {

			path = path.toAbsolutePath().normalize();
			Path parent = path.getParent();

			Path metadata;
			String base;
			String localName;

			if(Files.isRegularFile(path)) {
				localName = path.getFileName().toString();
				
				metadata = parent.resolve(localName + suffix);
				base = parent.normalize().toUri().toString();		
			} else {
				throw new RuntimeException("Folder metadata not supported (yet)");
			}
			
			Model m = ModelFactory.createDefaultModel();
			if(Files.exists(metadata)) {
				RDFDataMgr.read(m, metadata.toString(), base, Lang.TURTLE);
			}

			result = m.createResource(base + localName);
		} else {
			result = null;
		}


		return result;		
	}

	public Resource setRecord(Path path, Resource info) {
		
		Resource result;
		
		if(Files.exists(path)) {

			path = path.toAbsolutePath().normalize();
			Path parent = path.getParent();
			
			Path metadata;
			String base;
			String localName;
			
			if(Files.isRegularFile(path)) {
				localName = path.getFileName().toString();
				
				metadata = parent.resolve(localName + suffix);
				base = parent.normalize().toUri().toString();
			} else { // if(Files.isDirectory(path)) {
//				path.resolve("_folder.meta.ttl");
//				base = metadata.normalize().toUri().toString();
				// TODO Decide on the local name for folders
				throw new RuntimeException("Folder metadata not supported (yet)");
			}

			
			Model copy = ModelFactory.createDefaultModel();
			copy.add(info.getModel());
			Resource cpy = info.inModel(copy);


			result = ResourceUtils.renameResource(cpy, base + localName);

			if(cpy.getModel().isEmpty()) {
				try {
					Files.delete(metadata);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				org.apache.jena.rdf.model.RDFWriter writer = copy.getWriter("ttl-nb");
				try {
					writer.write(copy, Files.newOutputStream(metadata), base);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		} else {
			result = null;
		}
		
		return result;
	}

	@Override
	public Collection<Path> isAnnotationFor(Path path) {
		Collection<Path> result;
		String fileName = path.getFileName().toString();
		
		String suffix = ".meta";
		if(fileName.endsWith(suffix)) {
			Path parent = path.getParent();
			String targetFileName = fileName.substring(0, fileName.length() - suffix.length());
			Path tmp = parent.resolve(targetFileName);
			result = Collections.singleton(tmp);
		} else {
			result = Collections.emptySet();
		}
		
		return result;
	}
	
	
	
//	public static void main(String[] args) {
//		TurtleNoBaseTest.initTurtleWithoutBaseUri();
//		
//		PathAnnotatorRdfImpl annotator = new PathAnnotatorRdfImpl();
//		
//		Path p = Paths.get("/home/raven/.dcat/test2/c/c.ttl");
//		
//		RdfFileEntity<?> entity = annotator.getEntity(p);
//		entity.getInfo().addLiteral(RDF.value, "fooar");
//		entity.writeInfo();
//		
//		
////		Resource x = ModelFactory.createDefaultModel().createResource()
////			.addLiteral(DCAT.byteSize, 123);
////		
////		Resource r;
////		r = sys.getRecord(p);
////		System.out.println(r);
////		
////		System.out.println(sys.setRecord(p, x));
////		
////		System.out.println(sys.getRecord(p));
////		
////		RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
//	}
}
